package org.github.alexanderknop.jknish.parser;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.scanner.Token;
import org.github.alexanderknop.jknish.scanner.TokenType;

import java.util.ArrayList;
import java.util.List;

import static org.github.alexanderknop.jknish.scanner.TokenType.*;

public class Parser {
    public static List<Statement> parse(List<Token> tokens, ErrorReporter reporter) {
        Parser parser = new Parser(tokens, reporter);
        return parser.parse();
    }

    private final List<Token> tokens;
    private final ErrorReporter reporter;

    private int current = 0;

    private Parser(List<Token> tokens, ErrorReporter reporter) {
        this.tokens = tokens;
        this.reporter = reporter;
    }

    private List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            try {
                statements.add(statement());
            } catch (ParseError error) {
                synchronize();
            }
        }
        return statements;
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case VAR:
                case IF:
                case WHILE:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private Statement statement() {
        if (match(LEFT_BRACE)) return new Statement.Block(previous().line, block());
        if (match(IF)) return ifStatement();
        if (match(WHILE)) return whileStatement();
        if (match(RETURN)) return returnStatement();
        if (match(VAR)) return varStatement();

        return expressionStatement();
    }

    private Statement varStatement() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expression initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");

        return new Statement.Var(name.line, name.lexeme, initializer);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Statement ifStatement() {
        int line = previous().line;
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Statement.If(line, condition, thenBranch, elseBranch);
    }

    private Statement whileStatement() {
        int line = previous().line;
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Statement body = statement();

        return new Statement.While(line, condition, body);
    }

    private Statement returnStatement() {
        int line = previous().line;
        Expression value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Statement.Return(line, value);
    }

    private Statement expressionStatement() {
        int line = peek().line;

        Expression expression = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expression(line, expression);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expression = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                String name = ((Expression.Variable) expression).name;
                return new Expression.Assign(equals.line, name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    private Expression or() {
        Expression expression = and();

        while (match(OR)) {
            int line = previous().line;
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(line,
                    expression, LogicalOperator.operator(operator.type), right);
        }

        return expression;
    }

    private Expression and() {
        Expression expression = equality();

        while (match(AND)) {
            int line = previous().line;
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(line,
                    expression, LogicalOperator.operator(operator.type), right);
        }

        return expression;
    }

    private Expression equality() {
        Expression expression = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Call(operator.line,
                    expression, operator.lexeme, right);
        }

        return expression;
    }

    private Expression comparison() {
        Expression expression = addition();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = addition();
            expression = new Expression.Call(operator.line,
                    expression, operator.lexeme, right);
        }

        return expression;
    }

    private Expression addition() {
        Expression expression = multiplication();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expression right = multiplication();
            expression = new Expression.Call(operator.line,
                    expression, operator.lexeme, right);
        }

        return expression;
    }

    private Expression multiplication() {
        Expression expression = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Call(operator.line,
                    expression, operator.lexeme, right);
        }

        return expression;
    }

    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Call(operator.line,
                    right, operator.lexeme);
        }

        return call();
    }

    private Expression call() {
        Expression expression = primary();

        while (match(DOT)) {
            Token name = consume(IDENTIFIER, "Expect property name after '.'.");
            if (match(LEFT_PAREN)) {
                expression = finishCall(previous().line, name, expression);
            } else {
                expression = new Expression.Call(name.line, expression, name.lexeme);
            }
        }

        return expression;
    }

    private Expression finishCall(int line, Token name, Expression callee) {
        List<Expression> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() > 255) {
                    error(peek(), "Cannot have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expression.Call(line, callee, name.lexeme, arguments);
    }

    private Expression primary() {
        if (match(FALSE)) return new Expression.Literal(previous().line, false);
        if (match(TRUE)) return new Expression.Literal(previous().line, true);

        if (match(NIL)) return new Expression.Literal(previous().line, null);

        if (match(NUMBER, STRING)) {
            return new Expression.Literal(previous().line, previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expression.Variable(previous().line, previous().lexeme);
        }

        if (match(LEFT_PAREN)) {
            Expression expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return expression;
        }

        throw error(peek(), "Expect expression.");
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private ParseError error(Token token, String message) {
        reporter.error(token, message);
        return new ParseError();
    }

    private static class ParseError extends RuntimeException {
    }
}