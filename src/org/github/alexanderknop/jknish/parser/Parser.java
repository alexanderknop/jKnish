package org.github.alexanderknop.jknish.parser;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.scanner.Token;
import org.github.alexanderknop.jknish.scanner.TokenType;

import java.util.ArrayList;
import java.util.List;

import static org.github.alexanderknop.jknish.scanner.TokenType.*;

public final class Parser {
    public static Statement.Block parse(List<Token> tokens, KnishErrorReporter reporter) {
        Parser parser = new Parser(tokens, reporter);
        return parser.parse();
    }

    private final List<Token> tokens;
    private final KnishErrorReporter reporter;

    private int current = 0;

    private Parser(List<Token> tokens, KnishErrorReporter reporter) {
        this.tokens = tokens;
        this.reporter = reporter;
    }

    private Statement.Block parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            try {
                statements.add(statement());
            } catch (ParseError error) {
                synchronize();
            }
        }
        return new Statement.Block(0, statements);
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
        if (match(VAR)) return varStatement();
        if (match(CLASS)) return classStatement();
        if (match(RETURN)) return returnStatement();

        return expressionStatement();
    }

    private Statement.Return returnStatement() {
        int line = previous().line;
        Expression value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return statement.");
        return new Statement.Return(line, value);
    }

    private Statement classStatement() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' to begin class definition.");
        List<Statement.Method> methods = new ArrayList<>();
        List<Statement.Method> constructors = new ArrayList<>();
        List<Statement.Method> staticMethods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            if (match(STATIC)) {
                staticMethods.add(methodStatement());
            } else if (match(CONSTRUCT)) {
                constructors.add(methodStatement());
            } else {
                methods.add(methodStatement());
            }
        }
        consume(RIGHT_BRACE, "Expect '}' after class definition.");
        return new Statement.Class(name.line, name.lexeme, staticMethods, constructors, methods);
    }

    private Statement.Method methodStatement() {
        Token nameToken = advance();
        String name = nameToken.lexeme;

        int maxNumberOfArguments;
        int minNumberOfArguments;
        boolean canBeGetter;

        switch (nameToken.type) {
            case IDENTIFIER -> {
                if (match(EQUAL)) {
                    maxNumberOfArguments = 1;
                    minNumberOfArguments = 1;
                    canBeGetter = false;
                    name += "=";
                } else {
                    maxNumberOfArguments = Integer.MAX_VALUE;
                    minNumberOfArguments = 0;
                    canBeGetter = true;
                }
            }

            case PLUS,
                    STAR, SLASH, PERCENT,
                    GREATER, GREATER_EQUAL,
                    LESS, LESS_EQUAL,
                    EQUAL_EQUAL, BANG_EQUAL -> {
                maxNumberOfArguments = 1;
                minNumberOfArguments = 1;
                canBeGetter = false;
            }
            case MINUS -> {
                maxNumberOfArguments = 1;
                minNumberOfArguments = 1;
                canBeGetter = true;
            }

            default -> throw error(nameToken, "Expect method or operator name.");
        }

        List<String> argumentsNames = null;
        if (match(LEFT_PAREN)) {
            argumentsNames = methodParameters(minNumberOfArguments, maxNumberOfArguments);
        } else if (!canBeGetter) {
            throw error(peek(), "Expect '(' after operator name.");
        }

        consume(LEFT_BRACE, "Expect '{' to begin method body.");
        List<Statement> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(statement());
        }
        consume(RIGHT_BRACE, "Expect '}' after class definition.");

        return new Statement.Method(nameToken.line,
                name, argumentsNames, statements);
    }

    private List<String> methodParameters(int minNumberOfArguments, int maxNumberOfArguments) {
        List<String> argumentsNames = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                Token argument = consume(IDENTIFIER, "Expect variable name.");
                argumentsNames.add(argument.lexeme);
            } while (match(COMMA) && argumentsNames.size() < maxNumberOfArguments);
        }

        if (argumentsNames.size() < minNumberOfArguments) {
            throw error(peek(), "Expect variable name.");
        }

        consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return argumentsNames;
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
            } else if (expression instanceof Expression.Field) {
                String name = ((Expression.Field) expression).name;
                return new Expression.AssignField(equals.line, name, value);
            } else if (expression instanceof Expression.StaticField) {
                String name = ((Expression.StaticField) expression).name;
                return new Expression.AssignStaticField(equals.line, name, value);
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

        while (match(BANG_EQUAL, EQUAL_EQUAL,
                BANG_EQUAL_EQUAL, EQUAL_EQUAL_EQUAL)) {
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

        while (match(SLASH, STAR, PERCENT)) {
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
            Token nameToken = consume(IDENTIFIER, "Expect property name after '.'.");
            String name = nameToken.lexeme;
            List<Expression> arguments = null;
            Statement.MethodBody block = null;

            if (match(EQUAL)) {
                arguments = new ArrayList<>();
                arguments.add(expression());
                name += "=";
            } else {
                if (match(LEFT_PAREN)) {
                    arguments = parseArguments();
                }

                if (match(LEFT_BRACE)) {
                    block = new Statement.MethodBody(nameToken.line,
                            parseBlockArguments(),
                            new Statement.Block(nameToken.line, block())
                    );
                }
            }

            expression = new Expression.Call(
                    nameToken.line, expression, name,
                    block,
                    arguments
            );
        }

        return expression;
    }

    private List<String> parseBlockArguments() {
        List<String> argumentsNames = null;
        if (match(VERTICAL)) {
            argumentsNames = new ArrayList<>();
            do {
                argumentsNames.add(consume(IDENTIFIER, "Expect variable name.").lexeme);
            } while (match(COMMA));
            consume(VERTICAL, "Expect '|' after block parameters.");
        }
        return argumentsNames;
    }

    private List<Expression> parseArguments() {
        List<Expression> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                arguments.add(expression());
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expect ')' after arguments.");

        return arguments;
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

        if (match(THIS)) {
            return new Expression.This(previous().line);
        }

        if (match(FIELD_IDENTIFIER)) {
            return new Expression.Field(previous().line, previous().lexeme);
        }

        if (match(STATIC_FIELD_IDENTIFIER)) {
            return new Expression.StaticField(previous().line, previous().lexeme);
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
