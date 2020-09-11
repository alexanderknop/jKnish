package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.KnishObject;
import org.github.alexanderknop.jknish.interpreter.objects.*;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;

import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter implements Expression.Visitor<KnishObject>, Statement.Visitor<Void> {

    public static void interpret(List<Statement> statements, Writer output, ErrorReporter reporter) {
        Environment globals = new Environment();
        globals.define("System", new KnishSystem(output));

        Interpreter interpreter = new Interpreter(statements, globals, reporter);

        interpreter.interpret();
    }

    private final List<Statement> statements;
    private final ErrorReporter reporter;

    private Environment environment;

    private Interpreter(List<Statement> statements, Environment globals, ErrorReporter reporter) {
        this.statements = statements;
        this.reporter = reporter;
        this.environment = globals;
    }

    private void interpret() {
        try {
            executeBlock(statements);
        } catch (KnishRuntimeException e) {
            reporter.error(e.line, e.message);
        }
    }

    private void executeBlock(List<Statement> statements) {
        for (Statement statement : statements) {
            execute(statement);
        }
    }

    private void execute(Statement statement) {
        if (statement != null) {
            statement.accept(this);
        }
    }

    private KnishObject evaluate(Expression expression) {
        if (expression == null) {
            return KnishNull.NULL;
        }
        return expression.accept(this);
    }

    @Override
    public KnishObject visitAssignExpression(Expression.Assign assign) {
        return environment.set(assign.variable, evaluate(assign.value));
    }

    @Override
    public KnishObject visitCallExpression(Expression.Call call) {
        List<KnishObject> arguments = null;
        if (call.arguments != null) {
            arguments = call.arguments.stream().map(this::evaluate).collect(Collectors.toList());
        }

        return evaluate(call.object).call(call.line, call.method, arguments);
    }

    @Override
    public KnishObject visitLiteralExpression(Expression.Literal literal) {
        if (literal == null) {
            return KnishNull.NULL;
        }

        if (literal.value instanceof Boolean) {
            return KnishBoolean.valueOf((Boolean) literal.value);
        }

        if (literal.value instanceof String) {
            return new KnishString((String) literal.value);
        }

        if (literal.value instanceof Long) {
            return KnishNumber.valueOf((Long) literal.value);
        }

        throw new UnsupportedOperationException("Unknown type of literal " + literal.value);
    }

    @Override
    public KnishObject visitVariableExpression(Expression.Variable variable) {
        return environment.get(variable.line, variable.name);
    }

    @Override
    public KnishObject visitLogicalExpression(Expression.Logical logical) {
        KnishObject left = evaluate(logical.left);
        if (left.equals(KnishNull.NULL)) {
            throw new KnishRuntimeException(logical.line, "Null pointer exception.");
        }

        return switch (logical.operator) {
            case AND -> {
                if (left.equals(KnishBoolean.TRUE)) {
                    KnishObject right = evaluate(logical.right);
                    if (!(right instanceof KnishBoolean)) {
                        throw new KnishRuntimeException(logical.line, "Null pointer exception.");
                    }
                    yield right;
                }
                yield KnishBoolean.FALSE;
            }
            case OR -> {
                if (left.equals(KnishBoolean.FALSE)) {
                    KnishObject right = evaluate(logical.right);
                    if (!(right instanceof KnishBoolean)) {
                        throw new KnishRuntimeException(logical.line, "Null pointer exception.");
                    }
                    yield right;
                }
                yield KnishBoolean.TRUE;
            }
        };
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression expression) {
        evaluate(expression.expression);
        return null;
    }

    @Override
    public Void visitorIfStatement(Statement.If anIf) {
        KnishObject conditionValue = evaluate(anIf.condition);

        if (conditionValue == KnishNull.NULL) {
            throw new KnishRuntimeException(anIf.line, "If condition cannot be nil.");
        }

        if (conditionValue instanceof KnishBoolean) {
            if (conditionValue.equals(KnishBoolean.TRUE)) {
                execute(anIf.thenBranch);
            } else {
                execute(anIf.elseBranch);
            }
            return null;
        }

        throw new KnishRuntimeException(anIf.line, "Condition must have type Boolean.");
    }

    @Override
    public Void visitWhileStatement(Statement.While aWhile) {
        KnishObject conditionValue = evaluate(aWhile.condition);

        while (true) {
            if (conditionValue == KnishNull.NULL) {
                throw new KnishRuntimeException(aWhile.line, "While condition cannot be nil.");
            }

            if (conditionValue instanceof KnishBoolean) {
                if (conditionValue.equals(KnishBoolean.TRUE)) {
                    execute(aWhile.body);
                } else {
                    break;
                }
            }
        }

        throw new KnishRuntimeException(aWhile.line, "Condition must have type Boolean.");
    }

    @Override
    public Void visitVarStatement(Statement.Var var) {
        environment.define(var.name, evaluate(var.initializer));
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return aReturn) {
        throw new UnsupportedOperationException("Return is not supported, yet.");
    }

    @Override
    public Void visitBlockStatement(Statement.Block block) {
        Environment previous = environment;
        environment = new Environment(environment);

        executeBlock(block.statements);

        environment = previous;
        return null;
    }
}
