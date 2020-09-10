package org.github.alexanderknop.jknish.scanner;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.github.alexanderknop.jknish.scanner.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ScannerTest {

    @Test
    void scanTokens() {
        test("var x = 1",
                List.of(VAR, IDENTIFIER, EQUAL, NUMBER, EOF),
                "Was not able to scan 'var x = 1'");
        test("var x=1",
                List.of(VAR, IDENTIFIER, EQUAL, NUMBER, EOF),
                "Spaces shouldn't matter in 'var x = 1'");
        test("var x = \n1",
                List.of(VAR, IDENTIFIER, EQUAL, NUMBER, EOF),
                "Line breaks shouldn't matter in 'var x = 1'");
        test("var x = 1 // this is a one line comment",
                List.of(VAR, IDENTIFIER, EQUAL, NUMBER, EOF),
                "Comments shouldn't matter in 'var x = 1'");
    }

    private void test(String source, List<TokenType> expected, String message) {
        List<Token> actualTokens = Scanner.tokens(source, null);
        assertNotNull(actualTokens, "The list of tokens cannot be null.");
        List<TokenType> types =
                actualTokens.stream()
                        .map(token -> token.type)
                        .collect(Collectors.toList());
        assertEquals(expected, types, message);
    }
}