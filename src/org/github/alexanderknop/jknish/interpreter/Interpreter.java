package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishModule;
import org.github.alexanderknop.jknish.objects.KnishObject;
import org.github.alexanderknop.jknish.objects.KnishRuntimeException;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;

import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter implements Expression.Visitor<KnishObject>, Statement.Visitor<Void> {

    public static void interpret(List<Statement> statements, Writer output, ErrorReporter reporter) {
        Environment globals = createEnvironment(new KnishCore(output));

        Interpreter interpreter = new Interpreter(statements, globals, reporter);

        interpreter.interpret();
    }

    private static Environment createEnvironment(KnishModule module) {
        Environment globals = new Environment();

        for (var object : module.getObjects().entrySet()) {
            globals.define(object.getKey(), object.getValue());
        }

        return globals;
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
        } catch (KnishRuntimeExceptionWithLine e) {
            reporter.error(e.getLine(), e.getMessage());
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
            return KnishCore.KnishNull.NULL;
        }
        return expression.accept(this);
    }

    @Override
    public KnishObject visitAssignExpression(Expression.Assign assign) {
        return environment.set(assign.line, assign.variable, evaluate(assign.value));
    }

    @Override
    public KnishObject visitCallExpression(Expression.Call call) {
        List<KnishObject> arguments = null;
        if (call.arguments != null) {
            arguments = call.arguments.stream().map(this::evaluate).collect(Collectors.toList());
        }

        try {
            return evaluate(call.object).call(call.method, arguments);
        } catch (KnishRuntimeException e) {
            throw new KnishRuntimeExceptionWithLine(call.line, e);
        }
    }

    @Override
    public KnishObject visitLiteralExpression(Expression.Literal literal) {
        if (literal.value == null) {
            return KnishCore.KnishNull.NULL;
        }

        if (literal.value instanceof Boolean) {
            return KnishCore.KnishBoolean.valueOf((Boolean) literal.value);
        }

        if (literal.value instanceof String) {
            return new KnishCore.KnishString((String) literal.value);
        }

        if (literal.value instanceof Long) {
            return KnishCore.KnishNumber.valueOf((Long) literal.value);
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
        if (!(left instanceof KnishCore.KnishBoolean)) {
            throw new KnishRuntimeExceptionWithLine(logical.line, "Left operand must be boolean.");
        }

        return switch (logical.operator) {
            case AND -> {
                if (left.equals(KnishCore.KnishBoolean.TRUE)) {
                    KnishObject right = evaluate(logical.right);
                    if (!(right instanceof KnishCore.KnishBoolean)) {
                        throw new KnishRuntimeExceptionWithLine(logical.line, "Right operand must be boolean.");
                    }
                    yield right;
                }
                yield KnishCore.KnishBoolean.FALSE;
            }
            case OR -> {
                if (left.equals(KnishCore.KnishBoolean.FALSE)) {
                    KnishObject right = evaluate(logical.right);
                    if (!(right instanceof KnishCore.KnishBoolean)) {
                        throw new KnishRuntimeExceptionWithLine(logical.line, "Right operand must be boolean.");
                    }
                    yield right;
                }
                yield KnishCore.KnishBoolean.TRUE;
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

        if (conditionValue == KnishCore.KnishNull.NULL) {
            throw new KnishRuntimeExceptionWithLine(anIf.line, "If condition cannot be nil.");
        }

        if (conditionValue instanceof KnishCore.KnishBoolean) {
            if (conditionValue.equals(KnishCore.KnishBoolean.TRUE)) {
                execute(anIf.thenBranch);
            } else {
                execute(anIf.elseBranch);
            }
            return null;
        }

        throw new KnishRuntimeExceptionWithLine(anIf.line, "Condition must have type Boolean.");
    }

    @Override
    public Void visitWhileStatement(Statement.While aWhile) {
        while (true) {
            KnishObject conditionValue = evaluate(aWhile.condition);
            if (conditionValue == KnishCore.KnishNull.NULL) {
                throw new KnishRuntimeExceptionWithLine(aWhile.line, "While condition cannot be nil.");
            } else if (conditionValue instanceof KnishCore.KnishBoolean) {
                if (conditionValue.equals(KnishCore.KnishBoolean.TRUE)) {
                    execute(aWhile.body);
                } else {
                    break;
                }
            } else {
                throw new KnishRuntimeExceptionWithLine(aWhile.line, "Condition must have type Boolean.");
            }
        }

        return null;
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
