package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import static java.util.Collections.emptyList;
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
    void testClassStaticMethods() {
        testCorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                List.of(
                                        new Statement.Method(2,
                                                "test",
                                                null,
                                                List.of(
                                                        new Statement.Expression(3,
                                                                new Expression.Call(3,
                                                                        new Expression.Variable(3,
                                                                                "System"),
                                                                        "print",
                                                                        new Expression.Literal(3,
                                                                                "Hello world!")
                                                                )
                                                        )
                                                )
                                        )
                                ), emptyList(), emptyList()
                        ),
                        new Statement.Expression(4,
                                new Expression.Call(4,
                                        new Expression.Variable(4, "Test"),
                                        "test"
                                )
                        )
                )
        );

        testIncorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(), emptyList()
                        ),
                        new Statement.Expression(4,
                                new Expression.Call(4,
                                        new Expression.Variable(4, "Test"),
                                        "test"
                                )
                        )
                ),
                "[line 4] Error: An object does not implement test."
        );
    }

    @Test
    void testClassMethods() {
        testCorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test",
                                                null,
                                                List.of(
                                                        new Statement.Expression(3,
                                                                new Expression.Call(3,
                                                                        new Expression.Variable(3,
                                                                                "System"),
                                                                        "print",
                                                                        new Expression.Literal(3,
                                                                                "Hello world!")
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(4,
                                new Expression.Call(4,
                                        new Expression.Call(4,
                                                new Expression.Variable(4, "Test"),
                                                "new",
                                                emptyList()
                                        ),
                                        "test"
                                )
                        )
                )
        );

        testCorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test",
                                                List.of("x"),
                                                List.of(
                                                        new Statement.Expression(3,
                                                                new Expression.Call(3,
                                                                        new Expression.Variable(3,
                                                                                "x"),
                                                                        "+",
                                                                        new Expression.Literal(3,
                                                                                1L)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(4,
                                new Expression.Call(4,
                                        new Expression.Call(4,
                                                new Expression.Variable(4, "Test"),
                                                "new",
                                                emptyList()
                                        ),
                                        "test",
                                        new Expression.Literal(4, 1L)
                                )
                        )
                )
        );

        testIncorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test",
                                                List.of("x"),
                                                List.of(
                                                        new Statement.Expression(3,
                                                                new Expression.Call(3,
                                                                        new Expression.Variable(3,
                                                                                "x"),
                                                                        "+",
                                                                        new Expression.Literal(3,
                                                                                1L)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(4,
                                new Expression.Call(4,
                                        new Expression.Call(4,
                                                new Expression.Variable(4, "Test"),
                                                "new",
                                                emptyList()
                                        ),
                                        "test",
                                        new Expression.Literal(4, "Hello")
                                )
                        )
                ),
                "[line 4] Error: The value of 0th argument has incompatible type."
        );

        testIncorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(), emptyList()
                        ),
                        new Statement.Expression(4,
                                new Expression.Call(4,
                                        new Expression.Call(4,
                                                new Expression.Variable(4, "Test"),
                                                "new",
                                                emptyList()
                                        ),
                                        "test"
                                )
                        )
                ),
                "[line 4] Error: An object does not implement test."
        );
    }

    @Test
    void testThis() {
        testCorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test1",
                                                null,
                                                List.of(
                                                        new Statement.Expression(3,
                                                                new Expression.Call(3,
                                                                        new Expression.Variable(3,
                                                                                "System"),
                                                                        "print",
                                                                        new Expression.Literal(3,
                                                                                "Hello world!")
                                                                )
                                                        )
                                                )
                                        ),
                                        new Statement.Method(4,
                                                "test2",
                                                emptyList(),
                                                List.of(
                                                        new Statement.Expression(5,
                                                                new Expression.Call(5,
                                                                        new Expression.Variable(5,
                                                                                "this"),
                                                                        "test1"
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(6,
                                new Expression.Call(6,
                                        new Expression.Call(6,
                                                new Expression.Variable(4, "Test"),
                                                "new",
                                                emptyList()
                                        ),
                                        "test2",
                                        emptyList()
                                )
                        )
                )
        );

        testCorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test1",
                                                null,
                                                List.of(
                                                        new Statement.Expression(3,
                                                                new Expression.Call(3,
                                                                        new Expression.Variable(3,
                                                                                "System"),
                                                                        "print",
                                                                        new Expression.Literal(3,
                                                                                "Hello world!")
                                                                )
                                                        )
                                                )
                                        ),
                                        new Statement.Method(4,
                                                "test2",
                                                null,
                                                List.of(
                                                        new Statement.Expression(5,
                                                                new Expression.Call(5,
                                                                        new Expression.Variable(5,
                                                                                "this"),
                                                                        "test1"
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(6,
                                new Expression.Call(6,
                                        new Expression.Call(6,
                                                new Expression.Variable(4, "Test"),
                                                "new",
                                                emptyList()
                                        ),
                                        "test2"
                                )
                        )
                )
        );

        testIncorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test1",
                                                null,
                                                List.of(
                                                        new Statement.Expression(3,
                                                                new Expression.Call(3,
                                                                        new Expression.Variable(3,
                                                                                "System"),
                                                                        "print",
                                                                        new Expression.Literal(3,
                                                                                "Hello world!")
                                                                )
                                                        )
                                                )
                                        ),
                                        new Statement.Method(4,
                                                "test2",
                                                emptyList(),
                                                List.of(
                                                        new Statement.Expression(5,
                                                                new Expression.Call(5,
                                                                        new Expression.Variable(5,
                                                                                "this"),
                                                                        "test"
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(6,
                                new Expression.Call(6,
                                        new Expression.Call(6,
                                                new Expression.Variable(4, "Test"),
                                                "new",
                                                emptyList()
                                        ),
                                        "test2",
                                        emptyList()
                                )
                        )
                ),
                "[line 1] Error: Incompatible constraints on Test."
        );
    }

    @Test
    void testReturn() {
        testCorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test",
                                                null,
                                                List.of(
                                                        new Statement.Return(3,
                                                                new Expression.Literal(3,
                                                                        "Hello world!")
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(4,
                                new Expression.Call(4,
                                        new Expression.Variable(4, "System"),
                                        "print",
                                        new Expression.Call(4,
                                                new Expression.Call(4,
                                                        new Expression.Variable(4, "Test"),
                                                        "new",
                                                        emptyList()
                                                ),
                                                "test"
                                        )
                                )
                        )
                )
        );

        testCorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test",
                                                null,
                                                List.of(
                                                        new Statement.Return(3,
                                                                new Expression.Literal(3,
                                                                        "Hello world!")
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Block(4,
                                List.of(
                                        new Statement.Var(5, "x",
                                                new Expression.Literal(5,
                                                        "Hello world!")),
                                        new Statement.Expression(6,
                                                new Expression.Call(6,
                                                        new Expression.Variable(6, "System"),
                                                        "print",
                                                        new Expression.Call(6,
                                                                new Expression.Call(6,
                                                                        new Expression.Variable(6, "Test"),
                                                                        "new",
                                                                        emptyList()
                                                                ),
                                                                "test"
                                                        )
                                                )
                                        ),
                                        new Statement.Expression(7,
                                                new Expression.Call(7,
                                                        new Expression.Variable(7, "System"),
                                                        "print",
                                                        new Expression.Variable(7, "x")
                                                )
                                        )
                                )
                        )
                )
        );

        testCorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test",
                                                null,
                                                List.of(
                                                        new Statement.If(3,
                                                                new Expression.Literal(3, Boolean.TRUE),
                                                                new Statement.Return(3,
                                                                        new Expression.Literal(3,
                                                                                "Hello world!")
                                                                ),
                                                                new Statement.Return(3,
                                                                        new Expression.Literal(3,
                                                                                1L)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(4,
                                new Expression.Call(4,
                                        new Expression.Variable(4, "System"),
                                        "print",
                                        new Expression.Call(4,
                                                new Expression.Call(4,
                                                        new Expression.Variable(4, "Test"),
                                                        "new",
                                                        emptyList()
                                                ),
                                                "test"
                                        )
                                )
                        )
                )
        );

        testIncorrect(
                List.of(
                        new Statement.Class(1,
                                "Test",
                                emptyList(), emptyList(),
                                List.of(
                                        new Statement.Method(2,
                                                "test",
                                                null,
                                                List.of(
                                                        new Statement.If(3,
                                                                new Expression.Literal(3, Boolean.TRUE),
                                                                new Statement.Return(3,
                                                                        new Expression.Literal(3,
                                                                                "Hello world!")
                                                                ),
                                                                new Statement.Return(3,
                                                                        new Expression.Literal(3,
                                                                                1L)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        new Statement.Expression(4,
                                new Expression.Call(4,
                                        new Expression.Call(4,
                                                new Expression.Call(4,
                                                        new Expression.Variable(4, "Test"),
                                                        "new",
                                                        emptyList()
                                                ),
                                                "test"
                                        ),
                                        "+",
                                        new Expression.Literal(4, 1L)
                                )
                        )
                ),
                "[line 4] Error: The value of 0th argument has incompatible type."
        );
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
        KnishErrorReporter reporter = new KnishErrorReporter(errors);
        TypeChecker.check(new KnishCore(new StringWriter()), statements, reporter);
        assertFalse(reporter.hadError(), "The script is supposed to be correct:\n" +
                errors);
    }

    private void testIncorrect(List<Statement> statements, String expectedMessage) {
        Writer errors = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(errors);
        TypeChecker.check(new KnishCore(new StringWriter()), statements, reporter);
        assertTrue(reporter.hadError(), "The script is supposed to be incorrect");
        assertEquals(expectedMessage.strip(), errors.toString().strip(),
                "The error message is expected to be:\n" +
                        expectedMessage + "\ninstead of\n" + errors);
    }
}