package org.github.alexanderknop.jknish.parser;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.scanner.Token;
import org.github.alexanderknop.jknish.scanner.TokenBuilder;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    void testParseIfStatement() {
        TokenBuilder builder = new TokenBuilder();
        builder
                .anIf().leftParen().number(1).rightParen().nextLine()
                .aReturn().number(1).nextLine().semicolon()
                .rightBrace()
                .eof();


        List<Statement> expected =
                List.of(
                        new Statement.If(
                                1,
                                new Expression.Literal(1, 1),
                                new Statement.Return(2, new Expression.Literal(1, 1)),
                                null
                        )
                );

        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder
                .anIf().leftParen().number(1).rightParen().nextLine()
                .aReturn().number(1).nextLine().semicolon().nextLine()
                .anElse().aReturn().number(2).semicolon()
                .rightBrace().eof();


        expected =
                List.of(
                        new Statement.If(
                                1,
                                new Expression.Literal(1, 1),
                                new Statement.Return(2, new Expression.Literal(1, 1)),
                                new Statement.Return(3, new Expression.Literal(1, 2))
                        )
                );

        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder
                .anIf().leftParen().number(1).rightParen().nextLine()
                .var().identifier("x").nextLine().semicolon()
                .rightBrace().eof();


        testIncorrect(builder.tokens(), "[line 2] Error at 'var': Expect expression.\n");
    }

    @Test
    void testParseVarStatement() {
        TokenBuilder builder = new TokenBuilder();
        builder.var().identifier("x").equal().number(1).eof();

        testIncorrect(builder.tokens(),
                "[line 0] Error at end: Expect ';' after variable declaration.\n");

        builder = new TokenBuilder();
        builder.var().identifier("x").equal().number(1).semicolon().eof();
        List<Statement> expected =
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1))
                );

        testCorrect(expected, builder.tokens());
    }


    private void testCorrect(List<Statement> expected, List<Token> tokens) {
        StringWriter writer = new StringWriter();
        ErrorReporter reporter = new ErrorReporter(writer);

        List<Statement> result = Parser.parse(tokens, reporter);
        assertFalse(reporter.hadError(),
                "The sequence is supposed to be correct:\n" + writer.toString());
        assertEquals(expected, result);
    }

    private void testIncorrect(List<Token> tokens, String errorMessage) {
        StringWriter writer = new StringWriter();
        ErrorReporter reporter = new ErrorReporter(writer);
        Parser.parse(tokens, reporter);

        assertTrue(reporter.hadError(),
                "The sequence is supposed to be incorrect.");
        assertEquals(errorMessage, writer.toString(),
                "The error message is supposed to be '" +
                        errorMessage.strip() + "' instead of '" +
                        writer.toString().strip() + "'.");
    }
}