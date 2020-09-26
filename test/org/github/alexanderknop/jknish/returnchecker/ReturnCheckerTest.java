package org.github.alexanderknop.jknish.returnchecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression.Literal;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Block;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.If;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Method;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Return;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReturnCheckerTest {

    public static final int STATIC_THIS_ID = 2;
    public static final int THIS_ID = 1;
    public static final int TEST_ID = 0;

    @Test
    void testGlobalReturn() {
        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Return(1, null)
                        ),
                        emptyMap()
                ),
                "[line 1] Error: Cannot return from top-level code."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Return(1,
                                        new Literal(1, 1L)
                                )
                        ),
                        emptyMap()
                ),
                "[line 1] Error: Cannot return from top-level code."
        );

        Method goodMethod = new Method(2,
                null,
                new Block(2),
                emptyMap()
        );
        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_ID, "Test"),
                                Map.of(0,
                                        new ResolvedStatement.Class(1,
                                                Map.of(
                                                        new MethodId("test", null),
                                                        goodMethod
                                                ),
                                                emptyMap(),
                                                emptyMap(),
                                                emptyMap(),
                                                emptyMap(),
                                                THIS_ID, STATIC_THIS_ID
                                        )
                                ),
                                new Return(1, null)
                        ),
                        emptyMap()
                ),
                "[line 1] Error: Cannot return from top-level code."
        );
    }

    @Test
    void testMixedReturn() {
        Method badMethod1 = new Method(2,
                null,
                new Block(2,
                        new If(3,
                                new Literal(4,
                                        Boolean.TRUE
                                ),
                                new Return(3,
                                        null
                                ),
                                new Return(4,
                                        new Literal(4,
                                                1L
                                        )
                                )
                        )
                ),
                emptyMap()
        );
        Method badMethod2 = new Method(2,
                null,
                new Block(2,
                        new If(3,
                                new Literal(3,
                                        Boolean.TRUE
                                ),
                                new Return(3,
                                        new Literal(3,
                                                1L
                                        )
                                )
                        )
                ),
                emptyMap()
        );
        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_ID, "Test"),
                                Map.of(0,
                                        new ResolvedStatement.Class(1,
                                                emptyMap(),
                                                emptyMap(),
                                                Map.of(
                                                        new MethodId("test", null),
                                                        badMethod1
                                                ),
                                                emptyMap(),
                                                emptyMap(),
                                                THIS_ID, STATIC_THIS_ID
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 4] Error: Cannot return nothing and some value in the same method."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_ID, "Test"),
                                Map.of(0,
                                        new ResolvedStatement.Class(1,
                                                emptyMap(),
                                                Map.of(
                                                        new MethodId("test", null),
                                                        badMethod1
                                                ),
                                                emptyMap(),
                                                emptyMap(),
                                                emptyMap(),
                                                THIS_ID, STATIC_THIS_ID
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 4] Error: Cannot return nothing and some value in the same method."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_ID, "Test"),
                                Map.of(0,
                                        new ResolvedStatement.Class(1,
                                                Map.of(
                                                        new MethodId("test", null),
                                                        badMethod1
                                                ),
                                                emptyMap(),
                                                emptyMap(),
                                                emptyMap(),
                                                emptyMap(),
                                                THIS_ID, STATIC_THIS_ID
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 4] Error: Cannot return nothing and some value in the same method."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_ID, "Test"),
                                Map.of(0,
                                        new ResolvedStatement.Class(1,
                                                Map.of(
                                                        new MethodId("test", null),
                                                        badMethod2
                                                ),
                                                emptyMap(),
                                                emptyMap(),
                                                emptyMap(),
                                                emptyMap(),
                                                THIS_ID, STATIC_THIS_ID
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 2] Error: Cannot return nothing and some value in the same method."
        );
    }

    private void testIncorrect(ResolvedScript script, String message) {
        StringWriter errors = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(errors);
        ReturnChecker.check(script, reporter);

        assertTrue(reporter.hadError());
        assertEquals(message.strip(), errors.toString().strip());
    }
}