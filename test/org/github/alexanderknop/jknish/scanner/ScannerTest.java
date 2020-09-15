package org.github.alexanderknop.jknish.scanner;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.github.alexanderknop.jknish.scanner.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ScannerTest {

    @Test
    void testVar() {
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

    @Test
    void testIf() {
        test("if (true) 1",
                List.of(IF, LEFT_PAREN, TRUE, RIGHT_PAREN, NUMBER, EOF),
                "Was not able to scan 'if (true) 1'");
        test("if (true) 1 else 2",
                List.of(IF, LEFT_PAREN, TRUE, RIGHT_PAREN, NUMBER, ELSE, NUMBER, EOF),
                "Was not able to scan 'if (true) 1 else 2'");
    }

    @Test
    void testWhile() {
        test("while (true) 1",
                List.of(WHILE, LEFT_PAREN, TRUE, RIGHT_PAREN, NUMBER, EOF),
                "Was not able to scan 'while (true) 1'");
    }

    @Test
    void testClass() {
        test("class Test { test {return 1} }",
                List.of(CLASS, IDENTIFIER, LEFT_BRACE, IDENTIFIER,
                        LEFT_BRACE, RETURN, NUMBER, RIGHT_BRACE, RIGHT_BRACE, EOF),
                "Was not able to scan 'class Test { test {return 1} }'");
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