package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TypeCheckerTest {
    @Test
    void testPrint() {
        testCorrect(List.of(
                new Statement.Expression(1,
                        new Expression.Call(1,
                                new Expression.Variable(1, "System"),
                                "print",
                                new Expression.Literal(1, 1L)
                        )
                )
        ));

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Variable(1, "System"),
                                        "print"
                                )
                        )
                ),
                "[line 1] Error: An object does not implement print.");
    }

    @Test
    void testAddition() {
        testCorrect(List.of(
                new Statement.Expression(1,
                        new Expression.Call(1,
                                new Expression.Literal(1, 1L),
                                "+",
                                new Expression.Literal(1, 1L)
                        )
                )
        ));

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Literal(1, 1L),
                                        "+",
                                        new Expression.Literal(1, Boolean.TRUE)
                                )
                        )
                ),
                "[line 1] Error: The value of 0th argument has incompatible type."
        );

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Literal(1, Boolean.TRUE),
                                        "+",
                                        new Expression.Literal(1, 1L)
                                )
                        )
                ),
                "[line 1] Error: An object does not implement +(_)."
        );

        testCorrect(List.of(
                new Statement.Expression(1,
                        new Expression.Call(1,
                                new Expression.Literal(1, "Hello "),
                                "+",
                                new Expression.Literal(1, "World")
                        )
                )
        ));

        testIncorrect(
                List.of(
                        new Statement.Expression(1,
                                new Expression.Call(1,
                                        new Expression.Literal(1, "Hello "),
                                        "+",
                                        new Expression.Literal(1, 1L)
                                )
                        )
                ),
                "[line 1] Error: The value of 0th argument has incompatible type."
        );
    }

    @Test
    void testVar() {
        testCorrect(List.of(
                new Statement.Var(1, "x",
                        new Expression.Literal(1, 1L)
                ),
                new Statement.Expression(2,
                        new Expression.Call(2,
                                new Expression.Variable(2, "x"),
                                "+",
                                new Expression.Literal(2, 1L)
                        )
                )
        ));

        testCorrect(List.of(
                new Statement.Var(1, "x", null),
                new Statement.Expression(2,
                        new Expression.Call(2,
                                new Expression.Variable(2, "x"),
                                "+",
                                new Expression.Literal(2, 1L)
                        )
                )
        ));

        testCorrect(List.of(
                new Statement.Var(1, "x", null
                ),
                new Statement.Expression(2,
                        new Expression.Call(2,
                                new Expression.Variable(2, "System"),
                                "print",
                                new Expression.Variable(2, "x")
                        )
                )
        ));

        testCorrect(List.of(
                new Statement.Var(1, "x", null),
                new Statement.Expression(2,
                        new Expression.Call(2,
                                new Expression.Variable(2, "x"),
                                "+",
                                new Expression.Literal(2, "y")
                        )
                )
        ));

        testIncorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)
                        ),
                        new Statement.Expression(2,
                                new Expression.Call(2,
                                        new Expression.Variable(2, "x"),
                                        "+",
                                        new Expression.Literal(2, "s")
                                )
                        )
                ),
                "[line 2] Error: The value of 0th argument has incompatible type.");
    }

    @Test
    void testAssign() {
        testCorrect(List.of(
                new Statement.Var(1, "x",
                        new Expression.Literal(1, 1L)
                ),
                new Statement.Expression(2,
                        new Expression.Assign(2,
                                "x",
                                new Expression.Literal(2, 2L)
                        )
                ),
                new Statement.Expression(2,
                        new Expression.Call(2,
                                new Expression.Variable(2, "x"),
                                "+",
                                new Expression.Literal(2, 1L)
                        )
                )
        ));

        testCorrect(List.of(
                new Statement.Var(1, "x",
                        new Expression.Literal(1, 1L)
                ),
                new Statement.Expression(3,
                        new Expression.Assign(3,
                                "x",
                                new Expression.Call(3,
                                        new Expression.Variable(3, "x"),
                                        "+",
                                        new Expression.Literal(3, 1L)
                                )
                        )
                )
        ));

        testCorrect(List.of(
                new Statement.Var(1, "x",
                        new Expression.Literal(1, 1L)
                ),
                new Statement.Expression(2,
                        new Expression.Assign(2,
                                "x",
                                new Expression.Literal(2, "y")
                        )
                )
        ));

        testIncorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)
                        ),
                        new Statement.Expression(2,
                                new Expression.Assign(2,
                                        "x",
                                        new Expression.Literal(2, "y")
                                )
                        ),
                        new Statement.Expression(3,
                                new Expression.Call(3,
                                        new Expression.Variable(3, "x"),
                                        "+",
                                        new Expression.Literal(3, 1L)
                                )
                        )
                ),
                "[line 3] Error: The value of 0th argument has incompatible type."
        );

        testIncorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)
                        ),
                        new Statement.Expression(2,
                                new Expression.Assign(2,
                                        "x",
                                        new Expression.Literal(2, "y")
                                )
                        ),
                        new Statement.Expression(3,
                                new Expression.Call(3,
                                        new Expression.Variable(3, "x"),
                                        "+",
                                        new Expression.Literal(3, "y")
                                )
                        )
                ),
                "[line 3] Error: The value of 0th argument has incompatible type."
        );

        testIncorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)
                        ),
                        new Statement.Expression(2,
                                new Expression.Assign(2,
                                        "x",
                                        new Expression.Literal(2, Boolean.TRUE)
                                )
                        ),
                        new Statement.Expression(3,
                                new Expression.Call(3,
                                        new Expression.Variable(3, "x"),
                                        "+",
                                        new Expression.Literal(3, 1L)
                                )
                        )
                ),
                "[line 3] Error: An object does not implement +(_)."
        );

        testCorrect(List.of(
                new Statement.Var(1, "x",
                        new Expression.Literal(1, 1L)
                ),
                new Statement.Expression(2,
                        new Expression.Assign(2,
                                "x",
                                new Expression.Literal(2, "y")
                        )
                ),
                new Statement.Expression(1,
                        new Expression.Call(1,
                                new Expression.Variable(1, "System"),
                                "print",
                                new Expression.Variable(1, "x")
                        )
                )
        ));
    }

    @Test
    void testWhile() {
        testCorrect(List.of(
                new Statement.Var(1, "x",
                        new Expression.Literal(1, Boolean.TRUE)
                ),
                new Statement.While(2,
                        new Expression.Variable(2, "x"),
                        new Statement.Expression(3, new Expression.Literal(3, 1L))
                )
        ));
        testIncorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)
                        ),
                        new Statement.While(2,
                                new Expression.Variable(2, "x"),
                                new Statement.Expression(3, new Expression.Literal(3, 1L))
                        )
                ),
                "[line 2] Error: While conditions must have type Boolean."
        );
    }

    @Test
    void testIf() {
        testCorrect(List.of(
                new Statement.Var(1, "x",
                        new Expression.Literal(1, Boolean.TRUE)
                ),
                new Statement.If(2,
                        new Expression.Variable(2, "x"),
                        new Statement.Expression(3, new Expression.Literal(3, 1L)),
                        new Statement.Expression(3, new Expression.Literal(3, 1L))
                )
        ));
        testIncorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, 1L)
                        ),
                        new Statement.If(2,
                                new Expression.Variable(2, "x"),
                                new Statement.Expression(3, new Expression.Literal(3, 1L)),
                                new Statement.Expression(3, new Expression.Literal(3, 1L))
                        )
                ),
                "[line 2] Error: If conditions must have type Boolean."
        );
        testIncorrect(
                List.of(
                        new Statement.Var(1, "x",
                                new Expression.Literal(1, Boolean.TRUE)
                        ),
                        new Statement.Expression(2,
                                new Expression.Assign(2,
                                        "x",
                                        new Expression.Literal(2, 1L)
                                )
                        ),
                        new Statement.If(2,
                                new Expression.Variable(2, "x"),
                                new Statement.Expression(3, new Expression.Literal(3, 1L)),
                                new Statement.Expression(3, new Expression.Literal(3, 1L))
                        )
                ),
                "[line 2] Error: If conditions must have type Boolean."
        );
    }


    private void testCorrect(List<Statement> statements) {
        Writer errors = new StringWriter();
        ErrorReporter reporter = new ErrorReporter(errors);
        TypeChecker.check(new KnishCore(new StringWriter()), statements, reporter);
        assertFalse(reporter.hadError(), "The script is supposed to be correct:\n" +
                errors);
    }

    private void testIncorrect(List<Statement> statements, String expectedMessage) {
        Writer errors = new StringWriter();
        ErrorReporter reporter = new ErrorReporter(errors);
        TypeChecker.check(new KnishCore(new StringWriter()), statements, reporter);
        assertTrue(reporter.hadError(), "The script is supposed to be incorrect");
        assertEquals(expectedMessage.strip(), errors.toString().strip(),
                "The error message is expected to be:\n" +
                        expectedMessage + "\ninstead of\n" + errors);
    }
}