package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishModule;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.parser.Statement;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

public class TypeChecker {
    public static void check(KnishCore core, List<Statement> statements, KnishErrorReporter reporter) {
        TypeCheckerVisitor typeCheckerVisitor = new TypeCheckerVisitor(core, statements, reporter);

        typeCheckerVisitor.check();
    }

    private TypeChecker() {
    }

    private static class TypeCheckerVisitor implements Statement.Visitor<SimpleType>, Expression.Visitor<SimpleType> {
        private final KnishCore core;
        private final Constrainer constrainer = new Constrainer();

        private SimpleType numberType;
        private SimpleType booleanType;
        private SimpleType stringType;

        private final List<Statement> statements;
        private final KnishErrorReporter reporter;

        private final Stack<HashMap<String, SimpleType>> scopes = new Stack<>();

        private TypeCheckerVisitor(KnishCore core, List<Statement> statements, KnishErrorReporter reporter) {
            this.core = core;
            this.statements = statements;
            this.reporter = reporter;
        }

        private void check() {
            Map<KnishModule.Class, SimpleType> types = SimpleType.fromKnishModule(core);

            numberType = types.get(core.getClass("Number"));
            booleanType = types.get(core.getClass("Boolean"));
            stringType = types.get(core.getClass("String"));

            beginScope();

            for (var objectName : core.getObjects().keySet()) {
                scopes.peek().put(objectName, types.get(core.getObjectType(objectName)));
            }

            for (Statement statement : statements) {
                checkStatement(statement);
            }
            endScope();
        }

        private SimpleType expressionType(Expression expression) {
            return expression.accept(this);
        }

        private SimpleType checkStatement(Statement statement) {
            return statement.accept(this);
        }

        private void beginScope() {
            scopes.push(new HashMap<>());
        }

        private void endScope() {
            scopes.pop();
        }

        private SimpleType variableType(String variable) {
            for (int i = scopes.size() - 1; i >= 0; i--) {
                if (scopes.get(i).containsKey(variable)) {
                    return scopes.get(i).get(variable);
                }
            }

            return define(variable);
        }

        private SimpleType define(String name) {
            return define(name, new SimpleType.Variable());
        }

        private SimpleType define(String name, SimpleType type) {
            scopes.peek().put(name, type);
            return type;
        }

        @Override
        public SimpleType visitAssignExpression(Expression.Assign assign) {
            SimpleType valueType = expressionType(assign.value);
            SimpleType variableType = variableType(assign.variable);
            constrainer.constrain(valueType, variableType,
                    new TypeErrorMessage(reporter, assign.line,
                            "Wrong type of the value assigned to " +
                                    assign.variable + "."));
            return valueType;
        }

        @Override
        public SimpleType visitCallExpression(Expression.Call call) {
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
        public SimpleType visitLiteralExpression(Expression.Literal literal) {
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
        public SimpleType visitVariableExpression(Expression.Variable variable) {
            return variableType(variable.name);
        }

        @Override
        public SimpleType visitLogicalExpression(Expression.Logical logical) {
            constrainer.constrain(expressionType(logical.left), booleanType,
                    new TypeErrorMessage(reporter, logical.line,
                            "Left operand of " + logical.operator + " must have type Boolean."));
            constrainer.constrain(expressionType(logical.right), booleanType,
                    new TypeErrorMessage(reporter, logical.line,
                            "Left operand of " + logical.operator + " must have type Boolean."));

            return null;
        }

        @Override
        public SimpleType visitExpressionStatement(Statement.Expression expression) {
            expressionType(expression.expression);
            return SimpleType.bottom();
        }

        @Override
        public SimpleType visitorIfStatement(Statement.If anIf) {
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
        public SimpleType visitWhileStatement(Statement.While aWhile) {
            constrainer.constrain(expressionType(aWhile.condition), booleanType,
                    new TypeErrorMessage(reporter, aWhile.line,
                            "While conditions must have type Boolean."));
            return checkStatement(aWhile.body);
        }

        @Override
        public SimpleType visitVarStatement(Statement.Var var) {
            SimpleType variableType = define(var.name);
            if (var.initializer != null) {
                constrainer.constrain(expressionType(var.initializer), variableType,
                        // this error is impossible since we create a fresh variable
                        new TypeErrorMessage(reporter, var.line, ""));
            }
            return SimpleType.bottom();
        }

        @Override
        public SimpleType visitBlockStatement(Statement.Block block) {
            beginScope();
            SimpleType returnType = checkStatementsList(block.statements);
            endScope();
            return returnType;
        }

        private SimpleType checkStatementsList(List<Statement> statements) {
            SimpleType returnType = new SimpleType.Variable();
            for (Statement statement : statements) {
                constrainer.constrain(checkStatement(statement), returnType,
                        new TypeErrorMessage(reporter, statement.line,
                                "Incompatible return types"));
            }
            return returnType;
        }

        @Override
        public SimpleType visitClassStatement(Statement.Class klass) {

            SimpleType.Variable instanceType = new SimpleType.Variable();
            constrainer.constrain(new SimpleType.Class(getInstanceType(instanceType, klass.methods)),
                    instanceType,
                    new TypeErrorMessage(reporter, klass.line,
                            "Incompatible constraints on " + klass.name + "."));


            SimpleType classType = variableType(klass.name);
            Map<MethodId, SimpleType.Method> staticMethods = getInstanceType(classType, klass.staticMethods);
            // todo: add support of constructors
            staticMethods.put(new MethodId("new", 0),
                    new SimpleType.Method(emptyList(), instanceType));

            constrainer.constrain(new SimpleType.Class(staticMethods),
                    classType,
                    new TypeErrorMessage(reporter, klass.line,
                            "Incompatible constraints on " + klass.name + " metaclass."));

            return SimpleType.bottom();
        }

        @Override
        public SimpleType visitReturnStatement(Statement.Return aReturn) {
            if (aReturn.value != null) {
                return expressionType(aReturn.value);
            }
            return SimpleType.bottom();
        }

        private Map<MethodId, SimpleType.Method> getInstanceType(SimpleType instanceType,
                                                                 List<Statement.Method> methodStatements) {
            Map<MethodId, SimpleType.Method> methods = new HashMap<>();
            for (Statement.Method method : methodStatements) {
                methods.put(new MethodId(method.name, arityFromArgumentsList(method.argumentsNames)),
                        methodType(instanceType, method.argumentsNames, method.body));
            }
            return methods;
        }

        private SimpleType.Method methodType(SimpleType instanceType,
                                             List<String> argumentsNames, List<Statement> body) {
            beginScope();
            List<SimpleType> argumentTypes = argumentsNames ==
                    null ? null :
                    argumentsNames.stream()
                            .map(this::define)
                            .collect(Collectors.toList());
            define("this", instanceType);

            SimpleType returnType = checkStatementsList(body);

            endScope();
            return new SimpleType.Method(argumentTypes, returnType);
        }
    }
}
