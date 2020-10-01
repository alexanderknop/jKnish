package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishObject;
import org.github.alexanderknop.jknish.objects.KnishRuntimeException;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.List;
import java.util.Map;

import static org.github.alexanderknop.jknish.parser.MethodId.processArgumentsList;

public final class Interpreter {
    public static void interpret(KnishCore core, ResolvedScript script, KnishErrorReporter reporter) {
        Environment globals = createEnvironment(core, script.globals);

        InterpreterVisitor interpreterVisitor = new InterpreterVisitor();

        try {
            interpreterVisitor.interpret(globals, script.code);
        } catch (RuntimeExceptionWithLine e) {
            reporter.error(e.getLine(), e.getMessage());
        }
    }

    private static Environment createEnvironment(KnishCore core,
                                                 Map<Integer, String> globalsIds) {
        Map<String, KnishObject> moduleObjects = core.getObjects();
        Environment globals = new Environment(globalsIds.keySet());

        globalsIds.forEach((id, name) -> globals.set(id, moduleObjects.get(name)));

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
                return KnishCore.nil();
            }

            if (literal.value instanceof Boolean) {
                return KnishCore.bool((Boolean) literal.value);
            }

            if (literal.value instanceof String) {
                return KnishCore.str((String) literal.value);
            }

            if (literal.value instanceof Long) {
                return KnishCore.num((Long) literal.value);
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
            if (!(left instanceof KnishCore.KnishBoolean)) {
                throw new RuntimeExceptionWithLine(logical.line, "Left operand must be boolean.");
            }

            return switch (logical.operator) {
                case AND -> {
                    if (left.equals(KnishCore.KnishBoolean.TRUE)) {
                        KnishObject right = evaluate(logical.right);
                        if (!(right instanceof KnishCore.KnishBoolean)) {
                            throw new RuntimeExceptionWithLine(logical.line, "Right operand must be boolean.");
                        }
                        yield right;
                    }
                    yield KnishCore.KnishBoolean.FALSE;
                }
                case OR -> {
                    if (left.equals(KnishCore.KnishBoolean.FALSE)) {
                        KnishObject right = evaluate(logical.right);
                        if (!(right instanceof KnishCore.KnishBoolean)) {
                            throw new RuntimeExceptionWithLine(logical.line, "Right operand must be boolean.");
                        }
                        yield right;
                    }
                    yield KnishCore.KnishBoolean.TRUE;
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

            if (conditionValue == KnishCore.nil()) {
                throw new RuntimeExceptionWithLine(anIf.line,
                        "If condition cannot be nil.");
            }

            if (conditionValue instanceof KnishCore.KnishBoolean) {
                if (conditionValue.equals(KnishCore.KnishBoolean.TRUE)) {
                    execute(anIf.thenBranch);
                } else {
                    execute(anIf.elseBranch);
                }
                return null;
            }

            throw new RuntimeExceptionWithLine(anIf.line,
                    "Condition must have type Boolean.");
        }

        @Override
        public Void visitWhileStatement(ResolvedStatement.While aWhile) {
            while (true) {
                KnishObject conditionValue = evaluate(aWhile.condition);
                if (conditionValue == KnishCore.nil()) {
                    throw new RuntimeExceptionWithLine(aWhile.line, "While condition cannot be nil.");
                } else if (conditionValue instanceof KnishCore.KnishBoolean) {
                    if (conditionValue.equals(KnishCore.KnishBoolean.TRUE)) {
                        execute(aWhile.body);
                    } else {
                        break;
                    }
                } else {
                    throw new RuntimeExceptionWithLine(aWhile.line, "Condition must have type Boolean.");
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
                                    klass, environment, this
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
            KnishObject value = KnishCore.nil();
            if (aReturn.value != null) {
                value = evaluate(aReturn.value);
            }

            throw new Return(value);
        }
    }
}
