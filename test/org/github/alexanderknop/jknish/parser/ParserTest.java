package org.github.alexanderknop.jknish.parser;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.scanner.Token;
import org.github.alexanderknop.jknish.scanner.TokenBuilder;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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
                .number(1).nextLine().semicolon()
                .eof();


        List<Statement> expected =
                List.of(
                        new Statement.If(
                                1,
                                new Expression.Literal(1, 1L),
                                new Statement.Expression(2, new Expression.Literal(2, 1L)),
                                null
                        )
                );

        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder
                .anIf().leftParen().number(1).rightParen().nextLine()
                .number(1).nextLine().semicolon().nextLine()
                .anElse().number(2).semicolon()
                .eof();


        expected =
                List.of(
                        new Statement.If(
                                1,
                                new Expression.Literal(1, 1L),
                                new Statement.Expression(2, new Expression.Literal(1, 1L)),
                                new Statement.Expression(3, new Expression.Literal(1, 2L))
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
                                        emptyList()
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

    @Test
    void testClassExpression() {
        TokenBuilder builder = new TokenBuilder();
        builder.aClass().identifier("T").leftBrace().nextLine()
                .identifier("method").leftBrace().nextLine()
                .rightBrace().nextLine()
                .rightBrace().nextLine().eof();
        List<Statement> expected =
                List.of(
                        new Statement.Class(0,
                                "T",
                                emptyList(), emptyList(), List.of(
                                new Statement.Method(1,
                                        "method",
                                        null,
                                        emptyList()
                                )
                        )
                        )
                );
        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.aClass().identifier("T").leftBrace().nextLine()
                .identifier("method").leftParen().identifier("argument").rightParen().leftBrace().nextLine()
                .rightBrace().nextLine()
                .rightBrace().nextLine().eof();
        expected =
                List.of(
                        new Statement.Class(0,
                                "T",
                                emptyList(), emptyList(), List.of(
                                new Statement.Method(1,
                                        "method",
                                        singletonList("argument"),
                                        emptyList()
                                )
                        )
                        )
                );
        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.aClass().identifier("T").leftBrace().nextLine()
                .identifier("method").leftParen()
                .identifier("argument1").comma().identifier("argument2")
                .rightParen().leftBrace().nextLine()
                .rightBrace().nextLine()
                .rightBrace().nextLine().eof();
        expected =
                List.of(
                        new Statement.Class(0,
                                "T",
                                emptyList(), emptyList(), List.of(
                                new Statement.Method(1,
                                        "method",
                                        List.of("argument1", "argument2"),
                                        emptyList()
                                )
                        )
                        )
                );
        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.aClass().identifier("T").leftBrace().nextLine()
                .aStatic().identifier("method")
                .leftParen().identifier("argument").rightParen().leftBrace().nextLine()
                .rightBrace().nextLine()
                .rightBrace().nextLine().eof();
        expected =
                List.of(
                        new Statement.Class(0,
                                "T",
                                List.of(
                                        new Statement.Method(1,
                                                "method",
                                                singletonList("argument"),
                                                emptyList()
                                        )
                                ), emptyList(), emptyList()
                        )
                );
        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.aClass().identifier("T").leftBrace().nextLine()
                .construct().identifier("method")
                .leftParen().identifier("argument").rightParen().leftBrace().nextLine()
                .rightBrace().nextLine()
                .rightBrace().nextLine().eof();
        expected =
                List.of(
                        new Statement.Class(0,
                                "T",
                                emptyList(),
                                List.of(
                                        new Statement.Method(1,
                                                "method",
                                                singletonList("argument"),
                                                emptyList()
                                        )
                                ),
                                emptyList()
                        )
                );
        testCorrect(expected, builder.tokens());

        builder = new TokenBuilder();
        builder.aClass().identifier("T").leftBrace().nextLine()
                .identifier("method").leftParen()
                .identifier("argument1")
                .leftBrace().nextLine()
                .rightBrace().nextLine()
                .rightBrace().nextLine().eof();

        testIncorrect(builder.tokens(), "[line 1] Error at '{': Expect ')' after arguments.");

        builder = new TokenBuilder();
        builder.aClass().identifier("T").leftBrace().nextLine()
                .identifier("method").leftParen()
                .identifier("argument1")
                .rightParen()
                .nextLine()
                .rightBrace().nextLine()
                .rightBrace().nextLine().eof();

        testIncorrect(builder.tokens(), "[line 2] Error at '}': Expect '{' to begin method body.");

        builder = new TokenBuilder();
        builder.aClass().identifier("T").leftBrace().nextLine()
                .identifier("method").leftParen()
                .identifier("argument1")
                .rightParen().leftBrace().nextLine()
                .nextLine()
                .rightBrace().nextLine().eof();

        testIncorrect(builder.tokens(), "[line 4] Error at end: Expect '}' after class definition.");

        builder = new TokenBuilder();
        builder.aClass().leftBrace().nextLine()
                .identifier("method").leftParen()
                .identifier("argument1")
                .rightParen().leftBrace().nextLine()
                .rightBrace().nextLine()
                .rightBrace().nextLine().eof();

        testIncorrect(builder.tokens(), "[line 0] Error at '{': Expect class name.");
    }

    private void testCorrect(List<Statement> expected, List<Token> tokens) {
        StringWriter writer = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(writer);

        Statement.Block result = Parser.parse(tokens, reporter);
        assertFalse(reporter.hadError(),
                "The sequence is supposed to be correct:\n" + writer.toString());
        assertEquals(expected, result.statements);
    }

    private void testIncorrect(List<Token> tokens, String errorMessage) {
        StringWriter writer = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(writer);
        Parser.parse(tokens, reporter);

        assertTrue(reporter.hadError(),
                "The sequence is supposed to be incorrect.");
        assertEquals(errorMessage.strip(), writer.toString().strip(),
                "The error message is supposed to be\n'" +
                        errorMessage.strip() + "' instead of\n'" +
                        writer.toString().strip() + "'.");
    }
}