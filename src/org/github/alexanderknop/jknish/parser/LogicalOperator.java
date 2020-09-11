package org.github.alexanderknop.jknish.parser;

import org.github.alexanderknop.jknish.scanner.TokenType;

import java.util.EnumMap;
import java.util.Map;

public enum LogicalOperator {
    AND, OR;

    static LogicalOperator operator(TokenType tokenType) {
        return tokenToOperator.get(tokenType);
    }

    private static final Map<TokenType, LogicalOperator> tokenToOperator = new EnumMap<>(TokenType.class);

    static {
        tokenToOperator.put(TokenType.AND, AND);
        tokenToOperator.put(TokenType.OR, OR);
    }
}
