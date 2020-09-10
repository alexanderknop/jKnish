package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;

import java.io.Writer;
import java.util.List;

public class Interpreter implements Expression.Visitor<Object>, Statement.Visitor<Void> {

    public static void interpret(List<Statement> statements, Writer output, ErrorReporter reporter) {
        Interpreter interpreter = new Interpreter(statements, output, reporter);

        interpreter.interpret();
    }

    private final List<Statement> statements;
    private final Writer output;
    private final ErrorReporter reporter;

    public Interpreter(List<Statement> statements, Writer output, ErrorReporter reporter) {
        this.statements = statements;
        this.output = output;
        this.reporter = reporter;
    }

    public void interpret() {
    }

    @Override
    public Object visitAssignExpression(Expression.Assign assign) {
        return null;
    }

    @Override
    public Object visitCallExpression(Expression.Call call) {
        return null;
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal literal) {
        return null;
    }

    @Override
    public Object visitVariableExpression(Expression.Variable variable) {
        return null;
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical logical) {
        return null;
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression expression) {
        return null;
    }

    @Override
    public Void visitorIfStatement(Statement.If anIf) {
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While aWhile) {
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var var) {
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return aReturn) {
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block block) {
        return null;
    }
}
