package org.github.alexanderknop.jknish.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.github.alexanderknop.jknish.scanner.TokenType.*;

public class TokenBuilder {
    private int line = 0;
    private final List<Token> tokensList = new ArrayList<>();

    public List<Token> tokens() {
        return Collections.unmodifiableList(tokensList);
    }

    public TokenBuilder var() {
        tokensList.add(new Token(VAR, "var", null, line));
        return this;
    }

    public TokenBuilder identifier(String name) {
        tokensList.add(new Token(IDENTIFIER, name, null, line));
        return this;
    }

    public TokenBuilder equal() {
        tokensList.add(new Token(EQUAL, "=", null, line));
        return this;
    }

    public TokenBuilder number(long v) {
        tokensList.add(new Token(NUMBER, Long.toString(v), v, line));
        return this;
    }

    public TokenBuilder nextLine() {
        line++;
        return this;
    }

    public TokenBuilder semicolon() {
        tokensList.add(new Token(SEMICOLON, ";", null, line));
        return this;
    }

    public TokenBuilder leftBrace() {
        tokensList.add(new Token(LEFT_BRACE, "{", null, line));
        return this;
    }

    public TokenBuilder rightBrace() {
        tokensList.add(new Token(RIGHT_BRACE, "}", null, line));
        return this;
    }

    public TokenBuilder aReturn() {
        tokensList.add(new Token(RETURN, "return", null, line));
        return this;
    }

    public TokenBuilder leftParen() {
        tokensList.add(new Token(LEFT_PAREN, "(", null, line));
        return this;
    }

    public TokenBuilder rightParen() {
        tokensList.add(new Token(RIGHT_PAREN, ")", null, line));
        return this;
    }

    public TokenBuilder anIf() {
        tokensList.add(new Token(IF, "if", null, line));
        return this;
    }

    public TokenBuilder anElse() {
        tokensList.add(new Token(ELSE, "if", null, line));
        return this;
    }

    public void eof() {
        tokensList.add(new Token(EOF, null, null, line));
    }
}
