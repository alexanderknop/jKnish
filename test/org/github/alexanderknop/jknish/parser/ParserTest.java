package org.github.alexanderknop.jknish.parser;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.scanner.Token;
import org.github.alexanderknop.jknish.scanner.TokenBuilder;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    void testParseWhileStatement() {
        TokenBuilder builder = new TokenBuilder();
        builder
                .aWhile().leftParen().number(1).rightParen().nextLine()
                .number(1).nextLine().semicolon()
                .eof();


        List<Statement> expected =
                List.of(
                        new Statement.While(
                                1,
                                new Expression.Literal(1, 1L),
                                new Statement.Expression(2, new Expression.Literal(2, 1L))
                        )
                );

        testCorrect(expected, builder.tokens());
    }

    @Test
    void testParseIfStatement() {
        TokenBuilder builder = new TokenBuilder();
        builder
                .anIf().leftParen().number(1).rightParen().nextLine()
                .aReturn().number(1).nextLine().semicolon()
                .eof();


        List<Statement> expected =
                List.of(
                        new Statement.If(
                                1,
                                new Expression.Literal(1, 1L),
                                new Statement.Return(2, new Expression.Literal(2, 1L)),
                                null
                        )
                );

        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder
                .anIf().leftParen().number(1).rightParen().nextLine()
                .aReturn().number(1).nextLine().semicolon().nextLine()
                .anElse().aReturn().number(2).semicolon()
                .eof();


        expected =
                List.of(
                        new Statement.If(
                                1,
                                new Expression.Literal(1, 1L),
                                new Statement.Return(2, new Expression.Literal(1, 1L)),
                                new Statement.Return(3, new Expression.Literal(1, 2L))
                        )
                );

        testCorrect(expected, builder.tokens());
    }

    @Test
    void testParseVarStatement() {
        TokenBuilder builder = new TokenBuilder();
        builder.var().identifier("x").equal().number(1).eof();

        testIncorrect(builder.tokens(),
                "[line 0] Error at end: Expect ';' after variable declaration.\n");

        builder = new TokenBuilder();
        builder.var().identifier("x").equal().number(1L).semicolon().eof();
        List<Statement> expected =
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L))
                );

        testCorrect(expected, builder.tokens());
    }

    @Test
    void testAdditionExpression() {
        TokenBuilder builder = new TokenBuilder();
        builder.identifier("x").plus().number(1L).semicolon().eof();
        List<Statement> expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "x"),
                                        "+",
                                        new Expression.Literal(1, 1L)))
                );

        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.identifier("x").minus().number(1L).semicolon().eof();
        expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "x"),
                                        "-",
                                        new Expression.Literal(1, 1L)))
                );

        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.identifier("x").plus().number(1L).star().number(2).semicolon().eof();
        expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "x"),
                                        "+",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, 1L),
                                                "*",
                                                new Expression.Literal(1, 2L)
                                        )
                                )
                        )
                );

        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.plus().identifier("x").semicolon().eof();

        testIncorrect(builder.tokens(),
                "[line 0] Error at '+': Expect expression.\n");

        builder = new TokenBuilder();
        builder.minus().identifier("x").semicolon().eof();
        expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "x"),
                                        "-"
                                )
                        )
                );

        testCorrect(expected, builder.tokens());
    }

    @Test
    void testMultiplicationExpression() {
        TokenBuilder builder = new TokenBuilder();
        builder.identifier("x").star().number(1L).semicolon().eof();
        List<Statement> expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "x"),
                                        "*",
                                        new Expression.Literal(1, 1L)))
                );
        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.identifier("x").slash().number(1L).star().number(2).semicolon().eof();
        expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Call(1,
                                                new Expression.Variable(1, "x"),
                                                "/",
                                                new Expression.Literal(1, 1L)),
                                        "*",
                                        new Expression.Literal(1, 2L)
                                )
                        )
                );


        testCorrect(expected, builder.tokens());
    }

    @Test
    void testCallExpression() {
        TokenBuilder builder = new TokenBuilder();
        builder.identifier("x").dot().identifier("method").semicolon().eof();
        List<Statement> expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "x"),
                                        "method"
                                )
                        )
                );
        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.identifier("x").dot().identifier("method").leftParen().rightParen().semicolon().eof();
        expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "x"),
                                        "method",
                                        Collections.emptyList()
                                )
                        )
                );
        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.identifier("x").dot().identifier("method").leftParen().number(1).rightParen().semicolon().eof();
        expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "x"),
                                        "method",
                                        List.of(
                                                new Expression.Literal(1, 1L)
                                        )
                                )
                        )
                );
        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.identifier("x").dot().identifier("method")
                .leftParen().number(1).comma().number(2).rightParen().semicolon().eof();
        expected =
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "x"),
                                        "method",
                                        List.of(
                                                new Expression.Literal(1, 1L),
                                                new Expression.Literal(1, 2L)
                                        )
                                )
                        )
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