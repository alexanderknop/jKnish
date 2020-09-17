package org.github.alexanderknop.jknish.resolver;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

class ResolverTest {
    @Test
    void testVar() {
        int systemVariable = 0;
        int xVariable = 1;
        int x2Variable = 2;
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
                        Map.of(systemVariable, "System")
                )
        );

        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Var(1, "x",
                                        new Expression.Literal(1, 1L))
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
                                        )
                                ),
                                Map.of(xVariable, "x"),
                                emptyMap()
                        ),
                        Map.of(systemVariable, "System")
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
                                                        new Expression.Literal(1, 1L))
                                        )
                                ),
                                new Statement.Expression(2,
                                        new Expression.Variable(2, "x")
                                )
                        )
                ),
                "[line 2] Error: Undeclared variable x."
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
                                        )
                                ),
                                Map.of(xVariable, "x"),
                                emptyMap()
                        ),
                        Map.of(systemVariable, "System")
                )
        );
    }

    private void testCorrect(Statement.Block statements, ResolvedScript expectedResolvedStatements) {
        KnishErrorReporter reporter = new KnishErrorReporter(new StringWriter());
        assertEquals(
                expectedResolvedStatements,
                Resolver.resolve(new KnishCore(new StringWriter()), statements, reporter)
        );

        assertFalse(reporter.hadError());
    }

    private void testIncorrect(Statement.Block statements, String message) {
        StringWriter errors = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(errors);
        Resolver.resolve(new KnishCore(new StringWriter()), statements, reporter);


        assertTrue(reporter.hadError());
        assertEquals(errors.toString().strip(), message.strip());
    }
}