package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishModule;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.*;

public final class TypeChecker {
    public static void check(ResolvedScript script, KnishErrorReporter reporter,
                             KnishModule... modules) {
        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(reporter);

        typeCheckerVisitor.check(script, modules);
    }

    private TypeChecker() {
    }

    private static final class TypeCheckerVisitor implements
            ResolvedStatement.Visitor<SimpleType>, ResolvedExpression.Visitor<SimpleType> {
        private final Constrainer constrainer = new Constrainer();

        private SimpleType numberType;
        private SimpleType booleanType;
        private SimpleType stringType;

        private final KnishErrorReporter reporter;

        private final Stack<HashMap<Integer, TypedVariableInformation>> scopes = new Stack<>();

        private TypeCheckerVisitor(KnishErrorReporter reporter) {
            this.reporter = reporter;
        }

        private void check(ResolvedScript script, KnishModule[] modules) {
            Map<KnishModule.Class, SimpleType> types =
                    SimpleType.fromKnishModule(KnishCore.core());
            Map<String, KnishModule.Class> objectTypes =
                    KnishCore.core().getObjectTypes();
            for (KnishModule module : modules) {
                types.putAll(SimpleType.fromKnishModule(module));
                objectTypes.putAll(module.getObjectTypes());
            }

            numberType = types.get(KnishCore.core().numType());
            booleanType = types.get(KnishCore.core().boolType());
            stringType = types.get(KnishCore.core().stringType());

            // define globals, we expect that all the globals are defined in core
            HashMap<Integer, TypedVariableInformation> newScope = new HashMap<>();
            script.globals.forEach((id, name) ->
                    {
                        KnishModule.Class objectType = objectTypes.get(name);
                        String className = null;
                        SimpleType simpleType = new SimpleType.Variable();

                        if (objectType != null) {
                            className =  objectType.getName();
                            simpleType = types.get(objectType);
                        } else {
                            reporter.error(0, "Unknown type of '" + name + "'.");
                        }

                        newScope.put(id,
                                new TypedVariableInformation(
                                        name,
                                        simpleType,
                                        className == null ? name : className
                                )
                        );
                    }
            );
            scopes.push(newScope);

            visitBlockStatement(script.code);
        }

        private SimpleType expressionType(ResolvedExpression expression) {
            return expression.accept(this);
        }

        private SimpleType checkStatement(ResolvedStatement statement) {
            return statement.accept(this);
        }

        private void beginScope(Map<Integer, String> names, Set<Integer> classVariables) {
            HashMap<Integer, TypedVariableInformation> newScope = new HashMap<>();
            names.forEach((id, name) ->
                    newScope.put(id,
                            new TypedVariableInformation(
                                    name,
                                    new SimpleType.Variable(),
                                    classVariables.contains(id) ? name + " metaclass" : null
                            )
                    )
            );
            scopes.push(newScope);
        }

        private void beginScope(Map<Integer, String> names, Map<Integer, String> classNames) {
            HashMap<Integer, TypedVariableInformation> newScope = new HashMap<>();
            names.forEach((id, name) ->
                    newScope.put(id,
                            new TypedVariableInformation(
                                    name,
                                    new SimpleType.Variable(),
                                    classNames.get(id)
                            )
                    )
            );
            scopes.push(newScope);
        }

        private void endScope() {
            scopes.pop();
        }

        private TypedVariableInformation variableInformation(int id) {
            for (int i = scopes.size() - 1; i >= 0; i--) {
                if (scopes.get(i).containsKey(id)) {
                    return scopes.get(i).get(id);
                }
            }

            throw new UnsupportedOperationException("Undefined variable with id equal to " + id);
        }

        private String variableName(int id) {
            return variableInformation(id).name;
        }

        private SimpleType variableType(int id) {
            return variableInformation(id).type;
        }

        @Override
        public SimpleType visitAssignExpression(ResolvedExpression.Assign assign) {
            SimpleType valueType = expressionType(assign.value);
            SimpleType variableType = variableType(assign.variableId);
            constrainer.constrain(valueType, variableType,
                    new TypeErrorMessage(reporter, assign.line,
                            "Wrong type of the value assigned to " +
                                    variableName(assign.variableId) + "."));
            return valueType;
        }

        @Override
        public SimpleType visitCallExpression(ResolvedExpression.Call call) {
            List<SimpleType> arguments;
            MethodId methodId;
            if (call.arguments != null) {
                methodId = new MethodId(call.method, call.arguments.size());
                arguments = new ArrayList<>();
                for (int i = 0; i < call.arguments.size(); i++) {
                    arguments.add(new SimpleType.Variable());
                }
            } else {
                methodId = new MethodId(call.method, null);
                arguments = null;
            }

            SimpleType.Variable value = new SimpleType.Variable();
            SimpleType.Class klass = new SimpleType.Class(
                    Map.of(methodId,
                            new SimpleType.Method(arguments, value))
            );

            String message = "An object does not implement " + methodId + ".";
            if (call.object instanceof ResolvedExpression.Variable) {
                ResolvedExpression.Variable object = (ResolvedExpression.Variable) call.object;
                if (variableInformation(object.variableId).className == null) {
                    message = "An object referred by the variable '" + variableName(object.variableId) +
                            "' does not implement " + methodId + ".";
                } else {
                    message = variableInformation(object.variableId).className + " does not implement " + methodId + ".";
                }
            }
            SimpleType objectType = expressionType(call.object);
            constrainer.constrain(objectType, klass,
                    new TypeErrorMessage(reporter, call.line, message));

            if (call.arguments != null) {
                for (int i = 0; i < call.arguments.size(); i++) {
                    constrainer.constrain(expressionType(call.arguments.get(i)), arguments.get(i),
                            new TypeErrorMessage(reporter, call.line,
                                    "The value of " + i + "th argument has incompatible type."));
                }
            }

            return value;
        }

        @Override
        public SimpleType visitLiteralExpression(ResolvedExpression.Literal literal) {
            if (literal.value == null) {
                return new SimpleType.Variable();
            } else if (literal.value instanceof Long) {
                return numberType;
            } else if (literal.value instanceof String) {
                return stringType;
            } else if (literal.value instanceof Boolean) {
                return booleanType;
            }

            throw new UnsupportedOperationException("Unknown type of the literal " + literal.value + ".");
        }

        @Override
        public SimpleType visitVariableExpression(ResolvedExpression.Variable variable) {
            return variableType(variable.variableId);
        }

        @Override
        public SimpleType visitLogicalExpression(ResolvedExpression.Logical logical) {
            constrainer.constrain(expressionType(logical.left), booleanType,
                    new TypeErrorMessage(reporter, logical.line,
                            "Left operand of " + logical.operator + " must have type Boolean."));
            constrainer.constrain(expressionType(logical.right), booleanType,
                    new TypeErrorMessage(reporter, logical.line,
                            "Left operand of " + logical.operator + " must have type Boolean."));

            return booleanType;
        }

        @Override
        public SimpleType visitExpressionStatement(ResolvedStatement.Expression expression) {
            expressionType(expression.resolvedExpression);
            return SimpleType.bottom();
        }

        @Override
        public SimpleType visitorIfStatement(ResolvedStatement.If anIf) {
            constrainer.constrain(expressionType(anIf.condition), booleanType,
                    new TypeErrorMessage(reporter, anIf.line,
                            "If conditions must have type Boolean."));
            SimpleType thenReturnType = checkStatement(anIf.thenBranch);
            SimpleType elseReturnType = SimpleType.bottom();
            if (anIf.elseBranch != null) {
                elseReturnType = checkStatement(anIf.elseBranch);
            }
            SimpleType returnType = new SimpleType.Variable();
            // the following error statements are impossible
            constrainer.constrain(thenReturnType, returnType,
                    new TypeErrorMessage(reporter, anIf.line, ""));
            constrainer.constrain(elseReturnType, returnType,
                    new TypeErrorMessage(reporter, anIf.line, ""));
            return returnType;
        }

        @Override
        public SimpleType visitWhileStatement(ResolvedStatement.While aWhile) {
            constrainer.constrain(expressionType(aWhile.condition), booleanType,
                    new TypeErrorMessage(reporter, aWhile.line,
                            "While conditions must have type Boolean."));
            return checkStatement(aWhile.body);
        }

        @Override
        public SimpleType visitBlockStatement(ResolvedStatement.Block block) {
            beginScope(block.names, block.classes.keySet());

            block.classes.forEach(this::defineClass);

            SimpleType returnType = new SimpleType.Variable();
            for (ResolvedStatement statement : block.resolvedStatements) {
                // this error is impossible since returnType is a fresh variable
                constrainer.constrain(checkStatement(statement), returnType,
                        new TypeErrorMessage(reporter, statement.line,
                                "Incompatible return types"));
            }

            endScope();

            return returnType;
        }

        @Override
        public SimpleType visitReturnStatement(ResolvedStatement.Return aReturn) {
            if (aReturn.value != null) {
                return expressionType(aReturn.value);
            }
            return SimpleType.bottom();
        }

        public void defineClass(int classId, ResolvedStatement.Class klass) {
            String className = variableName(classId);
            SimpleType metaClassType = variableType(classId);
            beginScope(klass.staticFields, Map.of(klass.staticThisId, className + " metaclass"));

            // make this of the metaclass to be of the same type as the metaclass
            constrainer.constrain(metaClassType, variableType(klass.staticThisId),
                    // this error is impossible since variableType(staticThisId) is fresh
                    new TypeErrorMessage(reporter, 0, ""));

            // restrict the list of static methods, before we check their types
            Map<MethodId, SimpleType.Method> staticMethods = new HashMap<>();
            klass.staticMethods.keySet().forEach(methodId ->
                    staticMethods.put(methodId, SimpleType.functionVariable(methodId.arity)));
            klass.constructors.keySet().forEach(methodId ->
                    staticMethods.put(methodId,
                            SimpleType.functionVariable(
                                    methodId.arity,
                                    SimpleType.classVariable(instanceSignature(klass).keySet())
                            )
                    )
            );
            constrainer.constrain(
                    new SimpleType.Class(staticMethods),
                    metaClassType,
                    // this error is impossible since variableName(classId) is fresh
                    new TypeErrorMessage(reporter, klass.line, "")
            );

            // add all the methods
            checkMethodTypes(klass.staticMethods, staticMethods);

            // add all the constructors
            klass.constructors.forEach((constructorId, constructor) -> {
                SimpleType.Variable classType = new SimpleType.Variable();

                // restrict the list of methods, before we check their types
                Map<MethodId, SimpleType.Method> methods = instanceSignature(klass);
                constrainer.constrain(
                        new SimpleType.Class(methods),
                        classType,
                        // this error is impossible since classType is fresh
                        new TypeErrorMessage(reporter, klass.line, "")
                );

                beginScope(klass.fields, Map.of(klass.thisId, className));
                // make this of the class to be of the same type as the class
                constrainer.constrain(classType, variableType(klass.thisId),
                        // this error is impossible since variableType(thisId) is fresh
                        new TypeErrorMessage(reporter, 0, ""));

                checkMethodTypes(klass.methods, methods);

                // merge constraints created by the constructor and by class methods
                checkMethod(
                        constructorId,
                        constructor.argumentsIds,
                        constructor.argumentNames,
                        constructor.body,
                        staticMethods.get(constructorId));


                constrainer.constrain(
                        classType,
                        staticMethods.get(constructorId).value,
                        new TypeErrorMessage(
                                reporter,
                                constructor.line,
                                "Incompatible constraints on the " +
                                        "instance of " + className + " returned by '" +
                                        constructorId + "'."
                        )
                );

                endScope();
            });

            endScope();
        }

        private Map<MethodId, SimpleType.Method> instanceSignature(ResolvedStatement.Class klass) {
            Map<MethodId, SimpleType.Method> methods = new HashMap<>();
            klass.methods.keySet().forEach(methodId ->
                    methods.put(methodId, SimpleType.functionVariable(methodId.arity)));
            methods.put(new MethodId("!==", 1),
                    new SimpleType.Method(List.of(SimpleType.top()), booleanType));
            methods.put(new MethodId("===", 1),
                    new SimpleType.Method(List.of(SimpleType.top()), booleanType));
            return methods;
        }

        private void checkMethodTypes(
                Map<MethodId, ResolvedStatement.Method> methods,
                Map<MethodId, SimpleType.Method> expectedTypes) {
            methods.forEach(
                    (methodId, method) ->
                            checkMethod(
                                    methodId,
                                    method.argumentsIds,
                                    method.argumentNames,
                                    method.body,
                                    expectedTypes.get(methodId)
                            )
            );
        }

        private void checkMethod(
                MethodId methodId,
                List<Integer> argumentsIds,
                Map<Integer, String> argumentsNames,
                ResolvedStatement.Block body, SimpleType.Method expectedType) {
            beginScope(argumentsNames, Collections.emptySet());

            SimpleType returnType = visitBlockStatement(body);
            List<SimpleType> argumentTypes =
                    MethodId.processArgumentsList(argumentsIds, this::variableType);

            endScope();
            constrainer.constrain(new SimpleType.Method(argumentTypes, returnType), expectedType,
                    new TypeErrorMessage(reporter, body.line,
                            "Incompatible constraints on '" + methodId + "'."));
        }
    }

    private static class TypedVariableInformation {
        public final String name;
        public final SimpleType type;
        public final String className;

        public TypedVariableInformation(String name, SimpleType type, String className) {
            this.name = name;
            this.type = type;
            this.className = className;
        }
    }
}
