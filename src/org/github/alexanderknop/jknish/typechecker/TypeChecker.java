package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishModule;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

public final class TypeChecker {
    public static void check(KnishCore core, ResolvedScript script, KnishErrorReporter reporter) {
        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(core, reporter);

        typeCheckerVisitor.check(script);
    }

    private TypeChecker() {
    }

    private static final class TypeCheckerVisitor implements
            ResolvedStatement.Visitor<SimpleType>, ResolvedExpression.Visitor<SimpleType> {
        private final KnishCore core;
        private final Constrainer constrainer = new Constrainer();

        private SimpleType numberType;
        private SimpleType booleanType;
        private SimpleType stringType;

        private final KnishErrorReporter reporter;

        private final Stack<HashMap<Integer, TypedVariableInformation>> scopes = new Stack<>();

        private TypeCheckerVisitor(KnishCore core, KnishErrorReporter reporter) {
            this.core = core;
            this.reporter = reporter;
        }

        private void check(ResolvedScript script) {
            Map<KnishModule.Class, SimpleType> types = SimpleType.fromKnishModule(core);

            numberType = types.get(core.getClass("Number"));
            booleanType = types.get(core.getClass("Boolean"));
            stringType = types.get(core.getClass("String"));

            // define globals, we expect that all the globals are defined in core
            HashMap<Integer, TypedVariableInformation> newScope = new HashMap<>();
            script.globals.forEach((id, name) -> newScope.put(id,
                    new TypedVariableInformation(name,
                            types.get(core.getObjectType(name))
                    )
            ));
            scopes.push(newScope);

            visitBlockStatement(script.code);
        }

        private SimpleType expressionType(ResolvedExpression expression) {
            return expression.accept(this);
        }

        private SimpleType checkStatement(ResolvedStatement statement) {
            return statement.accept(this);
        }

        private void beginScope(Map<Integer, String> names) {
            HashMap<Integer, TypedVariableInformation> newScope = new HashMap<>();
            for (var variable : names.entrySet()) {
                newScope.put(variable.getKey(),
                        new TypedVariableInformation(variable.getValue(),
                                new SimpleType.Variable()
                        )
                );
            }
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
                int arity = call.arguments.size();
                methodId = new MethodId(call.method, arity);
                arguments = new ArrayList<>();
                for (int i = 0; i < arity; i++) {
                    arguments.add(new SimpleType.Variable());
                }
            } else {
                methodId = new MethodId(call.method, null);
                arguments = null;
            }

            SimpleType.Variable value = new SimpleType.Variable();
            SimpleType.Class klass = new SimpleType.Class(Map.of(methodId,
                    new SimpleType.Method(arguments, value)));

            SimpleType objectType = expressionType(call.object);
            constrainer.constrain(objectType, klass,
                    new TypeErrorMessage(reporter, call.line,
                            "An object does not implement " + methodId + "."));

            if (call.arguments != null) {
                int arity = call.arguments.size();

                for (int i = 0; i < arity; i++) {
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

            return null;
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
            SimpleType elseReturnType = checkStatement(anIf.elseBranch);
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
            beginScope(block.names);

            block.classes.forEach(this::defineClass);

            SimpleType returnType = new SimpleType.Variable();
            for (ResolvedStatement statement : block.resolvedStatements) {
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
            beginScope(klass.staticFields);

            // make this of the metaclass to be of the same type as the metaclass
            constrainer.constrain(metaClassType, variableType(klass.staticThisId),
                    // this error is impossible since variableType(thisId) is fresh
                    new TypeErrorMessage(reporter, 0, ""));

            // add all the methods
            Map<MethodId, SimpleType.Method> staticMethods = methodsTypes(klass.staticMethods);
            // add all the constructors
            for (ResolvedStatement.Method constructor : klass.constructors) {
                SimpleType.Variable classType = new SimpleType.Variable();
                beginScope(klass.fields);
                // make this of the class to be of the same type as the class
                constrainer.constrain(classType, variableType(klass.thisId),
                        // this error is impossible since variableType(thisId) is fresh
                        new TypeErrorMessage(reporter, 0, ""));

                Map<MethodId, SimpleType.Method> methods = methodsTypes(klass.methods);

                // merge constraints created by the constructor and by class methods
                SimpleType.Method initializationType = methodType(
                        constructor.argumentsIds,
                        constructor.argumentNames,
                        constructor.body
                );

                SimpleType.Method constructorType;
                if (constructor.argumentsIds == null) {
                    constructorType = new SimpleType.Method(
                            null,
                            new SimpleType.Variable()
                    );
                } else {
                    constructorType = new SimpleType.Method(
                            IntStream.range(0, constructor.argumentsIds.size())
                                    .mapToObj(i -> new SimpleType.Variable())
                                    .collect(Collectors.toList()),
                            new SimpleType.Variable()
                    );
                }

                constrainer.constrain(
                        constructorType,
                        initializationType,
                        new TypeErrorMessage(
                                reporter,
                                constructor.line,
                                "Incompatible constraints on " +
                                        constructor.name + "."
                        )
                );

                String errorMessage =
                        "Incompatible constraints on " + className +
                                " created in " + constructor.name + ".";
                constrainer.constrain(
                        new SimpleType.Class(methods),
                        classType,
                        new TypeErrorMessage(reporter, constructor.line, errorMessage)
                );

                constrainer.constrain(
                        classType,
                        constructorType.value,
                        new TypeErrorMessage(
                                reporter,
                                constructor.line,
                                "Incompatible constraints on the " +
                                        "instance of " + className + " returned by " +
                                        constructor.name + "."
                        )
                );

                MethodId constructorId =
                        new MethodId(
                                constructor.name,
                                arityFromArgumentsList(constructor.argumentsIds)
                        );
                staticMethods.put(constructorId, constructorType);


                endScope();
            }

            String errorMessage =
                    "Incompatible constraints on " + className + " metaclass.";
            constrainer.constrain(
                    new SimpleType.Class(staticMethods),
                    metaClassType,
                    new TypeErrorMessage(reporter, klass.line, errorMessage)
            );

            endScope();
        }

        private Map<MethodId, SimpleType.Method> methodsTypes(
                List<ResolvedStatement.Method> methodStatements) {

            Map<MethodId, SimpleType.Method> methods = new HashMap<>();
            for (ResolvedStatement.Method method : methodStatements) {
                MethodId methodId =
                        new MethodId(method.name, arityFromArgumentsList(method.argumentsIds));
                methods.put(methodId,
                        methodType(method.argumentsIds,
                                method.argumentNames,
                                method.body)
                );
            }

            return methods;
        }

        private SimpleType.Method methodType(
                List<Integer> argumentsIds,
                Map<Integer, String> argumentsNames,
                ResolvedStatement.Block body) {
            beginScope(argumentsNames);

            SimpleType returnType = visitBlockStatement(body);

            List<SimpleType> argumentTypes =
                    argumentsIds == null ? null : argumentsIds.stream()
                            .map(this::variableType)
                            .collect(Collectors.toList());

            endScope();
            return new SimpleType.Method(argumentTypes, returnType);
        }
    }

    private static class TypedVariableInformation {
        public final String name;
        public final SimpleType type;

        public TypedVariableInformation(String name, SimpleType type) {
            this.name = name;
            this.type = type;
        }
    }
}
