package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.LogicalOperator;
import org.github.alexanderknop.jknish.parser.Statement;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
    @Test
    void testPrint() {
        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Literal(1, 1L)
                                )
                        )
                ),
                "1"
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print"
                                )
                        )
                ),
                "[line 1] Error: System metaclass does not implement 'print'."
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Literal(1, 1L),
                                        new Expression.Literal(1, 1L)
                                )
                        )
                ),
                "[line 1] Error: System metaclass does not implement 'print(_, _)'."
        );
    }

    @Test
    void testAddition() {
        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, 1L),
                                                "+",
                                                new Expression.Literal(1, 2L)
                                        )
                                )
                        )
                ),
                "3"
        );


        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, Boolean.TRUE),
                                                "+",
                                                new Expression.Literal(1, 2L)
                                        )
                                )
                        )
                ),
                "[line 1] Error: Boolean does not implement '+(_)'."
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, 1L),
                                                "+",
                                                new Expression.Literal(1, Boolean.TRUE)
                                        )
                                )
                        )
                ),
                "[line 1] Error: Right operand must be a number."
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, 1L),
                                                "+"
                                        )
                                )
                        )
                ),
                "[line 1] Error: Number does not implement '+'."
        );

        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, 1L),
                                                "-",
                                                new Expression.Literal(1, 2L)
                                        )
                                )
                        )
                ),
                "-1"
        );

        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, 1L),
                                                "-"
                                        )
                                )
                        )
                ),
                "-1"
        );

        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, "hello "),
                                                "+",
                                                new Expression.Literal(1, "world!")
                                        )
                                )
                        )
                ),
                "hello world!"
        );
    }

    @Test
    void testMultiplication() {
        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, 3L),
                                                "*",
                                                new Expression.Literal(1, 2L)
                                        )
                                )
                        )
                ),
                "6"
        );

        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, 6L),
                                                "/",
                                                new Expression.Literal(1, 2L)
                                        )
                                )
                        )
                ),
                "3"
        );


        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, Boolean.TRUE),
                                                "*",
                                                new Expression.Literal(1, 2L)
                                        )
                                )
                        )
                ),
                "[line 1] Error: Boolean does not implement '*(_)'."
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Call(1,
                                                new Expression.Literal(1, 1L),
                                                "*",
                                                new Expression.Literal(1, Boolean.TRUE)
                                        )
                                )
                        )
                ),
                "[line 1] Error: Right operand must be a number."
        );
    }

    @Test
    void testVar() {
        testCorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)),
                        new Statement.Expression(2,
                                new Expression.Call(2,
                                        new Expression.Variable(2, "System"),
                                        "print",
                                        new Expression.Variable(2, "x")
                                )
                        )
                ),
                "1"
        );

        testCorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)),
                        new Statement.Block(1,
                                List.of(
                                        new Statement.Var(1, "x",
                                                new Expression.Literal(1, 2L)),
                                        new Statement.Expression(2,
                                                new Expression.Call(2,
                                                        new Expression.Variable(2, "System"),
                                                        "print",
                                                        new Expression.Variable(2, "x")
                                                )
                                        )
                                )
                        )
                ),
                "2"
        );
    }

    @Test
    void testAssign() {
        testCorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)),
                        new Statement.Expression(2,
                                new Expression.Assign(2,
                                        "x",
                                        new Expression.Literal(2, 2L)
                                )
                        ),
                        new Statement.Expression(3,
                                new Expression.Call(3,
                                        new Expression.Variable(3, "System"),
                                        "print",
                                        new Expression.Variable(3, "x")
                                )
                        )
                ),
                "2"
        );

        testCorrect(
                List.of(
                        new Statement.Var(1, "x", null),
                        new Statement.Expression(2,
                                new Expression.Assign(2,
                                        "x",
                                        new Expression.Literal(2, 2L)
                                )
                        ),
                        new Statement.Expression(3,
                                new Expression.Call(3,
                                        new Expression.Variable(3, "System"),
                                        "print",
                                        new Expression.Variable(3, "x")
                                )
                        )
                ),
                "2"
        );

        testCorrect(
                List.of(
                        new Statement.Var(1, "x", null),
                        new Statement.Expression(2,
                                new Expression.Assign(2,
                                        "x",
                                        new Expression.Literal(2, 2L)
                                )
                        ),
                        new Statement.Expression(2,
                                new Expression.Assign(2,
                                        "x",
                                        new Expression.Literal(2, 3L)
                                )
                        ),
                        new Statement.Expression(3,
                                new Expression.Call(3,
                                        new Expression.Variable(3, "System"),
                                        "print",
                                        new Expression.Variable(3, "x")
                                )
                        )
                ),
                "3"
        );

        testCorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)),
                        new Statement.Block(1,
                                List.of(
                                        new Statement.Var(1, "x", null),
                                        new Statement.Expression(2,
                                                new Expression.Assign(2,
                                                        "x",
                                                        new Expression.Literal(2, 2L)
                                                )
                                        ),
                                        new Statement.Expression(2,
                                                new Expression.Call(2,
                                                        new Expression.Variable(2, "System"),
                                                        "print",
                                                        new Expression.Variable(2, "x")
                                                )
                                        )
                                )
                        )
                ),
                "2"
        );

        testCorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)),
                        new Statement.Block(1,
                                List.of(
                                        new Statement.Var(1, "x", null),
                                        new Statement.Expression(2,
                                                new Expression.Assign(2,
                                                        "x",
                                                        new Expression.Literal(2, 2L)
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(2,
                                new Expression.Call(2,
                                        new Expression.Variable(2, "System"),
                                        "print",
                                        new Expression.Variable(2, "x")
                                )
                        )
                ),
                "1"
        );

        testCorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)),
                        new Statement.Block(1,
                                List.of(
                                        new Statement.Expression(2,
                                                new Expression.Assign(2,
                                                        "x",
                                                        new Expression.Literal(2, 2L)
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(2,
                                new Expression.Call(2,
                                        new Expression.Variable(2, "System"),
                                        "print",
                                        new Expression.Variable(2, "x")
                                )
                        )
                ),
                "2"
        );

        testCorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)),
                        new Statement.Block(1,
                                List.of(
                                        new Statement.Expression(2,
                                                new Expression.Assign(2,
                                                        "x",
                                                        new Expression.Call(2,
                                                                new Expression.Variable(2, "x"),
                                                                "+",
                                                                new Expression.Literal(2, 1L)
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(3,
                                new Expression.Call(3,
                                        new Expression.Variable(3, "System"),
                                        "print",
                                        new Expression.Variable(3, "x")
                                )
                        )
                ),
                "2"
        );
    }

    @Test
    void testIf() {
        testCorrect(
                List.of(
                        new Statement.If(1,
                                new Expression.Literal(1, FALSE),
                                new Statement.Expression(2,
                                        new Expression.Call(2,
                                                new Expression.Variable(2, "System"),
                                                "print",
                                                new Expression.Literal(2, 1L)
                                        )
                                ),
                                new Statement.Expression(3,
                                        new Expression.Call(3,
                                                new Expression.Variable(3, "System"),
                                                "print",
                                                new Expression.Literal(3, 2L)
                                        )
                                )
                        )
                ),
                "2"
        );

        testCorrect(
                List.of(
                        new Statement.If(1,
                                new Expression.Literal(1, TRUE),
                                new Statement.Expression(2,
                                        new Expression.Call(2,
                                                new Expression.Variable(2, "System"),
                                                "print",
                                                new Expression.Literal(2, 1L)
                                        )
                                ),
                                null
                        )
                ),
                "1"
        );

        testCorrect(
                List.of(
                        new Statement.If(1,
                                new Expression.Literal(1, FALSE),
                                new Statement.Expression(2,
                                        new Expression.Call(2,
                                                new Expression.Variable(2, "System"),
                                                "print",
                                                new Expression.Literal(2, 1L)
                                        )
                                ),
                                null
                        )
                ),
                ""
        );

        testIncorrect(
                List.of(
                        new Statement.If(1,
                                new Expression.Literal(1, 1L),
                                new Statement.Expression(2,
                                        new Expression.Call(2,
                                                new Expression.Variable(2, "System"),
                                                "print",
                                                new Expression.Literal(2, 1L)
                                        )
                                ),
                                null
                        )
                ),
                "[line 1] Error: Condition must have type Boolean."
        );

        testIncorrect(
                List.of(
                        new Statement.If(1,
                                new Expression.Literal(1, null),
                                new Statement.Expression(2,
                                        new Expression.Call(2,
                                                new Expression.Variable(2, "System"),
                                                "print",
                                                new Expression.Literal(2, 1L)
                                        )
                                ),
                                null
                        )
                ),
                "[line 1] Error: If condition cannot be nil."
        );
    }

    @Test
    void testWhile() {
        testCorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 0L)),
                        new Statement.While(2,
                                new Expression.Call(2,
                                        new Expression.Variable(2, "x"),
                                        "<",
                                        new Expression.Literal(2, 3L)),
                                new Statement.Block(3,
                                        List.of(
                                                new Statement.Expression(4,
                                                        new Expression.Call(4,
                                                                new Expression.Variable(4, "System"),
                                                                "print",
                                                                new Expression.Variable(4, "x")
                                                        )
                                                ),
                                                new Statement.Expression(5,
                                                        new Expression.Assign(5,
                                                                "x",
                                                                new Expression.Call(5,
                                                                        new Expression.Variable(5, "x"),
                                                                        "+",
                                                                        new Expression.Literal(5, 1L)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                "0\n1\n2\n"
        );

        testIncorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 0L)),
                        new Statement.While(2,
                                new Expression.Variable(2, "x"),
                                new Statement.Block(3,
                                        List.of()
                                )
                        )
                ),
                "[line 2] Error: Condition must have type Boolean."
        );

        testIncorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, null)),
                        new Statement.While(2,
                                new Expression.Variable(2, "x"),
                                new Statement.Block(3,
                                        List.of()
                                )
                        )
                ),
                "[line 2] Error: While condition cannot be nil."
        );
    }

    @Test
    void testLogical() {
        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Logical(1,
                                                new Expression.Literal(1, TRUE),
                                                LogicalOperator.AND,
                                                new Expression.Literal(1, FALSE)
                                        )
                                )
                        )
                ),
                "false"
        );

        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Logical(1,
                                                new Expression.Literal(1, TRUE),
                                                LogicalOperator.OR,
                                                new Expression.Literal(1, FALSE)
                                        )
                                )
                        )
                ),
                "true"
        );

        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Logical(1,
                                                new Expression.Literal(1, TRUE),
                                                LogicalOperator.OR,
                                                new Expression.Literal(1, 1L)
                                        )
                                )
                        )
                ),
                "true"
        );

        testCorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Logical(1,
                                                new Expression.Literal(1, FALSE),
                                                LogicalOperator.AND,
                                                new Expression.Literal(1, 1L)
                                        )
                                )
                        )
                ),
                "false"
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Logical(1,
                                                new Expression.Literal(1, FALSE),
                                                LogicalOperator.OR,
                                                new Expression.Literal(1, 1L)
                                        )
                                )
                        )
                ),
                "[line 1] Error: Right operand must be boolean."
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Logical(1,
                                                new Expression.Literal(1, TRUE),
                                                LogicalOperator.AND,
                                                new Expression.Literal(1, 1L)
                                        )
                                )
                        )
                ),
                "[line 1] Error: Right operand must be boolean."
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Logical(1,
                                                new Expression.Literal(1, 1L),
                                                LogicalOperator.AND,
                                                new Expression.Literal(1, TRUE)
                                        )
                                )
                        )
                ),
                "[line 1] Error: Left operand must be boolean."
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print",
                                        new Expression.Logical(1,
                                                new Expression.Literal(1, 1L),
                                                LogicalOperator.OR,
                                                new Expression.Literal(1, FALSE)
                                        )
                                )
                        )
                ),
                "[line 1] Error: Left operand must be boolean."
        );
    }

    void testCorrect(List<Statement> statements, String expectedOutput) {
        StringWriter errorWriter = new StringWriter();
        ErrorReporter reporter = new ErrorReporter(errorWriter);

        StringWriter outputWriter = new StringWriter();


        Interpreter.interpret(statements, outputWriter, reporter);

        assertFalse(reporter.hadError(), "The script is correct;" +
                " the error message is:\n" + errorWriter.toString());
        String actual = outputWriter.toString().strip();
        assertEquals(expectedOutput.strip(), actual,
                "The output is supposed to be '" +
                        expectedOutput.strip() + "' instead of '" +
                        actual + "'.");
    }

    void testIncorrect(List<Statement> statements, String expectedError) {
        StringWriter error = new StringWriter();
        ErrorReporter reporter = new ErrorReporter(error);

        Interpreter.interpret(statements, new StringWriter(), reporter);

        assertTrue(reporter.hadError(), "The script is incorrect.");
        String actual = error.toString().strip();
        assertEquals(expectedError, actual,
                "The error message is supposed to be '" +
                        expectedError.strip() + "' instead of '" +
                        actual + "'.");
    }
}