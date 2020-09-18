package org.github.alexanderknop.jknish.resolver;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

class ResolverTest {

    public static final int SYSTEM_VARIABLE = 0;

    @Test
    void testVar() {
        int xVariable = 1;
        int x2Variable = 2;

        testIncorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Var(1, "x", null)
                        )
                ),
                "[line 1] Error: The variable x is defined, but never used."
        );

        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Var(1, "x", null),
                                new Statement.Expression(2,
                                        new Expression.Variable(2, "x")
                                )
                        )
                ),
                new ResolvedScript(
                        new ResolvedStatement.Block(0,
                                List.of(
                                        new ResolvedStatement.Expression(2,
                                                new ResolvedExpression.Variable(2, xVariable)
                                        )
                                ),
                                Map.of(xVariable, "x"),
                                emptyMap()
                        ),
                        // todo: fix the problem with multiple elements defined globally
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );

        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Var(1, "x",
                                        new Expression.Literal(1, 1L)),
                                new Statement.Expression(2,
                                        new Expression.Variable(2, "x")
                                )
                        )
                ),
                new ResolvedScript(
                        new ResolvedStatement.Block(0,
                                List.of(
                                        new ResolvedStatement.Expression(1,
                                                new ResolvedExpression.Assign(1,
                                                        xVariable,
                                                        new ResolvedExpression.Literal(1, 1L)
                                                )
                                        ),
                                        new ResolvedStatement.Expression(2,
                                                new ResolvedExpression.Variable(2, xVariable)
                                        )
                                ),
                                Map.of(xVariable, "x"),
                                emptyMap()
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );

        testIncorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Expression(1,
                                        new Expression.Variable(1, "x")
                                )
                        )
                ),
                "[line 1] Error: Undeclared variable x."
        );

        testIncorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Block(0,
                                        List.of(
                                                new Statement.Var(1, "x",
                                                        new Expression.Literal(1, 1L)),
                                                new Statement.Expression(2,
                                                        new Expression.Variable(2, "x")
                                                )
                                        )
                                ),
                                new Statement.Expression(3,
                                        new Expression.Variable(3, "x")
                                )
                        )
                ),
                "[line 3] Error: Undeclared variable x."
        );

        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Var(1, "x", null),
                                new Statement.Block(2,
                                        List.of(
                                                new Statement.Var(3, "x", null),
                                                new Statement.Expression(4,
                                                        new Expression.Variable(4, "x")
                                                )
                                        )
                                ),
                                new Statement.Expression(5,
                                        new Expression.Variable(5, "x")
                                )
                        )
                ),
                new ResolvedScript(
                        new ResolvedStatement.Block(0,
                                List.of(
                                        new ResolvedStatement.Block(2,
                                                List.of(
                                                        new ResolvedStatement.Expression(2,
                                                                new ResolvedExpression.Variable(2,
                                                                        x2Variable)
                                                        )
                                                ),
                                                Map.of(x2Variable, "x"),
                                                emptyMap()
                                        ),
                                        new ResolvedStatement.Expression(5,
                                                new ResolvedExpression.Variable(5,
                                                        xVariable)
                                        )
                                ),
                                Map.of(xVariable, "x"),
                                emptyMap()
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );
    }

    @Test
    void testIf() {
        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.If(1,
                                        new Expression.Literal(1, 1L),
                                        new Statement.Expression(2,
                                                new Expression.Literal(2, 1L)
                                        ),
                                        null
                                )
                        )
                ),
                new ResolvedScript(
                        new ResolvedStatement.Block(0,
                                List.of(
                                        new ResolvedStatement.If(1,
                                                new ResolvedExpression.Literal(1, 1L),
                                                new ResolvedStatement.Expression(2,
                                                        new ResolvedExpression.Literal(1, 1L)
                                                ),
                                                null
                                        )
                                ),
                                emptyMap(),
                                emptyMap()
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );
    }

    @Test
    void testCall() {
        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Expression(1,
                                        new Expression.Call(1,
                                                new Expression.Variable(1, "System"),
                                                "print",
                                                new Expression.Literal(1, 1L)
                                        )
                                )
                        )
                ),
                new ResolvedScript(
                        new ResolvedStatement.Block(0,
                                List.of(
                                        new ResolvedStatement.Expression(1,
                                                new ResolvedExpression.Call(1,
                                                        new ResolvedExpression.Variable(1,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new ResolvedExpression.Literal(1, 1L)
                                                )
                                        )
                                ),
                                emptyMap(),
                                emptyMap()
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );
    }

    @Test
    void testClass() {
        int testClass = 1;
        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Class(1,
                                        "Test",
                                        emptyList(),
                                        emptyList(),
                                        List.of(
                                                new Statement.Method(2,
                                                        "test",
                                                        null,
                                                        emptyList()
                                                )
                                        )
                                ),
                                new Statement.Expression(3,
                                        new Expression.Variable(3, "Test")
                                )
                        )
                ),
                new ResolvedScript(
                        new ResolvedStatement.Block(0,
                                List.of(
                                        new ResolvedStatement.Expression(3,
                                                new ResolvedExpression.Variable(3, testClass))
                                ),
                                Map.of(testClass, "Test"),
                                Map.of(testClass,
                                        new ResolvedStatement.Class(1,
                                                emptyList(),
                                                emptyList(),
                                                List.of(
                                                        new ResolvedStatement.Method(2,
                                                                "test",
                                                                null,
                                                                new ResolvedStatement.Block(2,
                                                                        emptyList(),
                                                                        emptyMap(),
                                                                        emptyMap()
                                                                ),
                                                                emptyMap()
                                                        )
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );

        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Class(1,
                                        "Test",
                                        List.of(
                                                new Statement.Method(2,
                                                        "test",
                                                        null,
                                                        emptyList()
                                                )
                                        ),
                                        emptyList(),
                                        emptyList()
                                ),
                                new Statement.Expression(3,
                                        new Expression.Variable(3, "Test")
                                )
                        )
                ),
                new ResolvedScript(
                        new ResolvedStatement.Block(0,
                                List.of(
                                        new ResolvedStatement.Expression(3,
                                                new ResolvedExpression.Variable(3, testClass))
                                ),
                                Map.of(testClass, "Test"),
                                Map.of(testClass,
                                        new ResolvedStatement.Class(1,
                                                List.of(
                                                        new ResolvedStatement.Method(2,
                                                                "test",
                                                                null,
                                                                new ResolvedStatement.Block(2,
                                                                        emptyList(),
                                                                        emptyMap(),
                                                                        emptyMap()
                                                                ),
                                                                emptyMap()
                                                        )
                                                ),
                                                emptyList(),
                                                emptyList()
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );

        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Class(1,
                                        "Test",
                                        emptyList(),
                                        List.of(
                                                new Statement.Method(2,
                                                        "test",
                                                        null,
                                                        emptyList()
                                                )
                                        ),
                                        emptyList()
                                ),
                                new Statement.Expression(3,
                                        new Expression.Variable(3, "Test")
                                )
                        )
                ),
                new ResolvedScript(
                        new ResolvedStatement.Block(0,
                                List.of(
                                        new ResolvedStatement.Expression(3,
                                                new ResolvedExpression.Variable(3, testClass))
                                ),
                                Map.of(testClass, "Test"),
                                Map.of(testClass,
                                        new ResolvedStatement.Class(1,
                                                emptyList(),
                                                List.of(
                                                        new ResolvedStatement.Method(2,
                                                                "test",
                                                                null,
                                                                new ResolvedStatement.Block(2,
                                                                        emptyList(),
                                                                        emptyMap(),
                                                                        emptyMap()
                                                                ),
                                                                emptyMap()
                                                        )
                                                ),
                                                emptyList()
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );
    }

    @Test
    void testAssign() {
        int testClass = 1;
        testIncorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Class(1,
                                        "Test",
                                        emptyList(),
                                        emptyList(),
                                        emptyList()
                                ),
                                new Statement.Expression(2,
                                        new Expression.Assign(2,
                                                "Test",
                                                new Expression.Literal(2, null)
                                        )
                                )
                        )
                ),
                "[line 2] Error: Cannot assign a new value to the class variable Test.\n" +
                        "[line 1] Error: The class Test is defined, but never used."
        );
    }

    private void testCorrect(Statement.Block statements, ResolvedScript expectedResolvedStatements) {
        StringWriter error = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(error);
        assertEquals(
                expectedResolvedStatements,
                Resolver.resolve(new KnishCore(new StringWriter()), statements, reporter)
        );

        assertFalse(reporter.hadError(), "The script is correct; however, the resolver reported:\n" +
                error.toString().strip());
    }

    private void testIncorrect(Statement.Block statements, String message) {
        StringWriter errors = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(errors);
        Resolver.resolve(new KnishCore(new StringWriter()), statements, reporter);


        assertTrue(reporter.hadError());
        assertEquals(message.strip(), errors.toString().strip());
    }
}