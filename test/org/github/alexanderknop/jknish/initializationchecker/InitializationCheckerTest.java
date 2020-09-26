package org.github.alexanderknop.jknish.initializationchecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression.Literal;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression.Variable;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Block;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Expression;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Method;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

class InitializationCheckerTest {

    private static final int X_VARIABLE_ID = 1;
    private static final int TEST_VARIABLE_ID = 2;
    public static final int THIS_ID = 3;
    public static final int STATIC_THIS_ID = 4;
    private static final int TEST_2_VARIABLE_ID = 5;
    public static final int THIS_2_ID = 6;
    public static final int STATIC_2_THIS_ID = 7;

    @Test
    void simpleTest() {
        testIncorrect(new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE_ID, "x"),
                                new Expression(1,
                                        new Variable(1,
                                                X_VARIABLE_ID
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 1] Error: Use of unassigned local variable 'x'."
        );
    }

    @Test
    void testWhile() {
        testIncorrect(new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE_ID, "x"),
                                new ResolvedStatement.While(1,
                                        new Literal(1, Boolean.FALSE),
                                        new Expression(2,
                                                new ResolvedExpression.Assign(2,
                                                        X_VARIABLE_ID,
                                                        new Literal(2,
                                                                1L
                                                        )
                                                )
                                        )
                                ),
                                new Expression(3,
                                        new Variable(3,
                                                X_VARIABLE_ID
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 3] Error: Use of unassigned local variable 'x'."
        );
    }

    @Test
    void testIf() {
        testIncorrect(new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE_ID, "x"),
                                new ResolvedStatement.If(1,
                                        new Literal(1, Boolean.TRUE),
                                        new Expression(2,
                                                new ResolvedExpression.Assign(2,
                                                        X_VARIABLE_ID,
                                                        new Literal(2,
                                                                1L
                                                        )
                                                )
                                        )
                                ),
                                new Expression(3,
                                        new Variable(3,
                                                X_VARIABLE_ID
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 3] Error: Use of unassigned local variable 'x'."
        );


        testCorrect(new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE_ID, "x"),
                                new ResolvedStatement.If(1,
                                        new Literal(1, Boolean.TRUE),
                                        new Expression(2,
                                                new ResolvedExpression.Assign(2,
                                                        X_VARIABLE_ID,
                                                        new Literal(2,
                                                                1L
                                                        )
                                                )
                                        ),
                                        new Expression(3,
                                                new ResolvedExpression.Assign(3,
                                                        X_VARIABLE_ID,
                                                        new Literal(3,
                                                                1L
                                                        )
                                                )
                                        )
                                ),
                                new Expression(4,
                                        new Variable(4,
                                                X_VARIABLE_ID
                                        )
                                )
                        ),
                        emptyMap()
                )
        );
    }

    @Test
    void testClass() {
        ResolvedStatement.Class testClass1 = new ResolvedStatement.Class(1,
                emptyList(),
                emptyList(),
                List.of(
                        new Method(2,
                                "test",
                                null,
                                new Block(2,
                                        new Expression(3,
                                                new Variable(3,
                                                        X_VARIABLE_ID
                                                )
                                        )
                                ),
                                emptyMap()
                        )
                ),
                emptyMap(),
                emptyMap(),
                THIS_ID, STATIC_THIS_ID
        );
        testIncorrect(new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE_ID, "x", TEST_VARIABLE_ID, "Test"),
                                Map.of(
                                        TEST_VARIABLE_ID,
                                        testClass1
                                ),
                                new Expression(4,
                                        new Variable(4, TEST_VARIABLE_ID)
                                )
                        ),
                        emptyMap()
                ),
                "[line 3] Error: Use of unassigned local variable 'x'."
        );

        ResolvedStatement.Class testClass2 = new ResolvedStatement.Class(1,
                emptyList(),
                emptyList(),
                List.of(
                        new Method(2,
                                "test",
                                null,
                                new Block(2,
                                        new Expression(3,
                                                new ResolvedExpression.Assign(3,
                                                        X_VARIABLE_ID,
                                                        new Literal(3, null)
                                                )
                                        )
                                ),
                                emptyMap()
                        )
                ),
                emptyMap(),
                emptyMap(),
                THIS_ID, STATIC_THIS_ID
        );
        testIncorrect(new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE_ID, "x", TEST_VARIABLE_ID, "Test"),
                                Map.of(
                                        TEST_VARIABLE_ID,
                                        testClass2
                                ),
                                new Expression(4,
                                        new Variable(4, TEST_VARIABLE_ID)
                                ),
                                new Expression(5,
                                        new Variable(5, X_VARIABLE_ID)
                                )
                        ),
                        emptyMap()
                ),
                "[line 5] Error: Use of unassigned local variable 'x'."
        );

        ResolvedStatement.Class testClass3 = new ResolvedStatement.Class(1,
                emptyList(),
                emptyList(),
                List.of(
                        new Method(2,
                                "test",
                                null,
                                new Block(2,
                                        new Expression(3,
                                                new Variable(3, TEST_2_VARIABLE_ID)
                                        )
                                ),
                                emptyMap()
                        )
                ),
                emptyMap(),
                emptyMap(),
                THIS_ID, STATIC_THIS_ID
        );
        ResolvedStatement.Class testClass4 = new ResolvedStatement.Class(1,
                emptyList(),
                emptyList(),
                List.of(
                        new Method(2,
                                "test",
                                null,
                                new Block(2,
                                        new Expression(3,
                                                new Variable(3, TEST_VARIABLE_ID)
                                        )
                                ),
                                emptyMap()
                        )
                ),
                emptyMap(),
                emptyMap(),
                THIS_2_ID, STATIC_2_THIS_ID
        );

        testCorrect(new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE_ID, "x", TEST_VARIABLE_ID, "Test"),
                                Map.of(
                                        TEST_VARIABLE_ID,
                                        testClass2
                                ),
                                new Expression(4,
                                        new Variable(4, TEST_VARIABLE_ID)
                                )
                        ),
                        emptyMap()
                )
        );
    }

    private void testCorrect(ResolvedScript script) {
        Writer errors = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(errors);
        InitializationChecker.check(script, reporter);
        assertFalse(reporter.hadError(),
                "The script is supposed to be incorrect: " + errors.toString());
    }

    private void testIncorrect(ResolvedScript script, String message) {
        Writer errors = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(errors);
        InitializationChecker.check(script, reporter);
        assertTrue(reporter.hadError(), "The script is supposed to be incorrect");
        assertEquals(message.strip(), errors.toString().strip(),
                "The error message is expected to be:\n" +
                        message + "\ninstead of\n" + errors);
    }
}