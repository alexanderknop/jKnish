package org.github.alexanderknop.jknish.scanner;

public enum TokenType {
    PRINT,

    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    IDENTIFIER,
    // Literals.
    STRING, NUMBER,
    NIL,
    TRUE, FALSE,

    // Keywords.
    VAR,
    AND, OR,
    IF, ELSE,
    WHILE,
    CLASS, STATIC, CONSTRUCT,
    RETURN,

    EOF
}