package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.*;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.github.alexanderknop.jknish.parser.MethodId.processArgumentsList;

public final class Interpreter {
    public static void interpret(ResolvedScript script, KnishErrorReporter reporter, KnishModule... modules) {
        Environment globals = createEnvironment(script.globals, modules);

        InterpreterVisitor interpreterVisitor = new InterpreterVisitor();

        try {
            interpreterVisitor.interpret(globals, script.code);
        } catch (RuntimeExceptionWithLine e) {
            reporter.error(e.getLine(), e.getMessage());
        }
    }

    private static Environment createEnvironment(Map<Integer, String> globalsIds, KnishModule... modules) {
        Environment globals = new Environment(globalsIds.keySet());

        Map<String, KnishObject> objects = new HashMap<>();

        KnishCore.core().getObjects().forEach(objects::put);
        Arrays.stream(modules).map(KnishModule::getObjects).forEach(objects::putAll);
        globalsIds.forEach((id, name) -> globals.set(id, objects.get(name)));

        return globals;
    }

    private Interpreter() {

    }

    static final class InterpreterVisitor implements
            ResolvedExpression.Visitor<KnishObject>, ResolvedStatement.Visitor<Void> {

        private Environment environment;

        private InterpreterVisitor() {
        }

        void interpret(Environment enclosing, ResolvedStatement.Block block) {
            Environment previous = environment;
            this.environment = enclosing;
            try {
                visitBlockStatement(block);
            } finally {
                environment = previous;
            }
        }

        private void execute(ResolvedStatement statement) {
            if (statement != null) {
                statement.accept(this);
            }
        }

        private KnishObject evaluate(ResolvedExpression expression) {
            return expression.accept(this);
        }

        @Override
        public KnishObject visitAssignExpression(ResolvedExpression.Assign assign) {
            return environment.set(assign.variableId, evaluate(assign.value));
        }

        @Override
        public KnishObject visitCallExpression(ResolvedExpression.Call call) {
            KnishObject object = evaluate(call.object);
            List<KnishObject> arguments =
                    processArgumentsList(call.arguments, this::evaluate);

            try {
                return object.call(call.method, arguments);
            } catch (RuntimeExceptionWithLine e) {
                // this is fine if we got a runtime error thrown by Knish
                throw e;
            } catch (KnishRuntimeException e) {
                // this is also fine if we got a runtime error thrown by a foreign object
                throw new RuntimeExceptionWithLine(call.line, e);
            } catch (Exception e) {
                // however, the foreign objects are allowed to throw only KnishRuntimeExceptions
                throw new RuntimeExceptionWithLine(call.line,
                        "Unknown exception with the message: " + e.getMessage());
            }
        }

        @Override
        public KnishObject visitLiteralExpression(ResolvedExpression.Literal literal) {
            if (literal.value == null) {
                return KnishCore.core().nil();
            }

            if (literal.value instanceof Boolean) {
                return KnishCore.core().bool((Boolean) literal.value);
            }

            if (literal.value instanceof String) {
                return KnishCore.core().str((String) literal.value);
            }

            if (literal.value instanceof Long) {
                return KnishCore.core().num((Long) literal.value);
            }

            throw new UnsupportedOperationException("Unknown type of literal " + literal.value);
        }

        @Override
        public KnishObject visitVariableExpression(ResolvedExpression.Variable variable) {
            return environment.get(variable.variableId);
        }

        @Override
        public KnishObject visitLogicalExpression(ResolvedExpression.Logical logical) {
            KnishObject left = evaluate(logical.left);
            boolean leftValue = KnishWrappedObject.unwrap(
                    left, Boolean.class,
                    "Left operand must be a wrapped Boolean.");

            return switch (logical.operator) {
                case AND -> {
                    if (leftValue) {
                        KnishObject right = evaluate(logical.right);
                        KnishWrappedObject.unwrap(
                                right, Boolean.class,
                                "Right operand must be a wrapped Boolean.");
                        yield right;
                    }
                    yield KnishCore.core().bool(false);
                }
                case OR -> {
                    if (!leftValue) {
                        KnishObject right = evaluate(logical.right);
                        KnishWrappedObject.unwrap(
                                right, Boolean.class,
                                "Right operand must be a wrapped Boolean.");
                        yield right;
                    }
                    yield KnishCore.core().bool(true);
                }
            };
        }

        @Override
        public Void visitExpressionStatement(ResolvedStatement.Expression expression) {
            evaluate(expression.resolvedExpression);
            return null;
        }

        @Override
        public Void visitorIfStatement(ResolvedStatement.If anIf) {
            KnishObject conditionValue = evaluate(anIf.condition);

            if (conditionValue == KnishCore.core().nil()) {
                throw new RuntimeExceptionWithLine(anIf.line,
                        "If condition cannot be nil.");
            }

            try {
                boolean value = KnishWrappedObject.unwrap(
                        conditionValue, Boolean.class,
                        "Condition must be a wrapped Boolean."
                );

                if (value) {
                    execute(anIf.thenBranch);
                } else {
                    execute(anIf.elseBranch);
                }
                return null;
            } catch (KnishRuntimeException e) {
                throw new RuntimeExceptionWithLine(anIf.line, e.getMessage());
            }
        }

        @Override
        public Void visitWhileStatement(ResolvedStatement.While aWhile) {
            while (true) {
                KnishObject conditionValue = evaluate(aWhile.condition);
                if (conditionValue == KnishCore.core().nil()) {
                    throw new RuntimeExceptionWithLine(aWhile.line,
                            "While condition cannot be nil.");
                } else if (conditionValue instanceof KnishWrappedObject<?> &&
                        ((KnishWrappedObject<?>) conditionValue).getValue() instanceof Boolean) {
                    if (conditionValue == KnishCore.core().bool(true)) {
                        execute(aWhile.body);
                    } else {
                        break;
                    }
                } else {
                    throw new RuntimeExceptionWithLine(aWhile.line,
                            "Condition must be a wrapped Boolean.");
                }
            }

            return null;
        }

        @Override
        public Void visitBlockStatement(ResolvedStatement.Block block) {
            Environment previous = environment;
            environment = new Environment(environment, block.names.keySet());
            block.classes.forEach((classId, klass) ->
                    environment.set(
                            classId,
                            new ClassInstance(
                                    block.names.get(classId),
                                    klass, environment, this,
                                    KnishCore.core().nil()
                            )
                    )
            );

            try {
                for (ResolvedStatement statement : block.resolvedStatements) {
                    execute(statement);
                }
            } finally {
                environment = previous;
            }

            return null;
        }

        @Override
        public Void visitReturnStatement(ResolvedStatement.Return aReturn) {
            KnishObject value = KnishCore.core().nil();
            if (aReturn.value != null) {
                value = evaluate(aReturn.value);
            }

            throw new Return(value);
        }
    }
}
