package org.github.alexanderknop.jknish.scanner;

import org.github.alexanderknop.jknish.KnishErrorReporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.github.alexanderknop.jknish.scanner.TokenType.*;

public final class Scanner {
    public static List<Token> tokens(String source, KnishErrorReporter reporter) {
        Scanner scanner = new Scanner(reporter, source);

        return scanner.tokens();
    }

    private final List<Token> tokenList = new ArrayList<>();
    private final KnishErrorReporter reporter;
    private final String source;

    private int start = 0;
    private int current = 0;
    private int line = 1;


    private Scanner(KnishErrorReporter reporter, String source) {
        this.reporter = reporter;
        this.source = source;
    }

    private List<Token> tokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            token();
        }

        tokenList.add(new Token(EOF, "", null, line));
        return tokenList;
    }

    private void token() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(LEFT_PAREN);
            case ')' -> addToken(RIGHT_PAREN);
            case '|' -> addToken(VERTICAL);
            case '{' -> addToken(LEFT_BRACE);
            case '}' -> addToken(RIGHT_BRACE);
            case ',' -> addToken(COMMA);
            case '.' -> addToken(DOT);
            case '-' -> addToken(MINUS);
            case '+' -> addToken(PLUS);
            case ';' -> addToken(SEMICOLON);
            case '*' -> addToken(STAR);

            case '!' -> addToken(match('=') ?
                    match('=') ?
                            BANG_EQUAL_EQUAL : BANG_EQUAL :
                    BANG);

            case '=' -> addToken(match('=') ?
                    match('=') ?
                            EQUAL_EQUAL_EQUAL : EQUAL_EQUAL :
                    EQUAL);
            case '<' -> addToken(match('=') ? LESS_EQUAL : LESS);
            case '>' -> addToken(match('=') ? GREATER_EQUAL : GREATER);

            case '"' -> string();

            case '/' -> {
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
            }

            case ' ', '\r', '\t' -> {
            }

            case '\n' -> line++;

            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    reporter.error(line, "Unexpected character.");
                }
            }
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // Unterminated string.
        if (isAtEnd()) {
            reporter.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        // See if the identifier is a reserved word.
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, IDENTIFIER);

        if (text.startsWith("__")) {
            type = STATIC_FIELD_IDENTIFIER;
        } else if (text.startsWith("_")) {
            type = FIELD_IDENTIFIER;
        }

        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }

        addToken(NUMBER,
                Long.parseLong(source.substring(start, current)));
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokenList.add(new Token(type, text, literal, line));
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("this", THIS);
        keywords.put("nil", NIL);
        keywords.put("true", TRUE);
        keywords.put("false", FALSE);
        keywords.put("var", VAR);
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("if", IF);
        keywords.put("else", ELSE);
        keywords.put("while", WHILE);
        keywords.put("class", CLASS);
        keywords.put("return", RETURN);
        keywords.put("static", STATIC);
        keywords.put("construct", CONSTRUCT);
    }
}
