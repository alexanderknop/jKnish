package org.github.alexanderknop.jknish.resolver;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression.Variable;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Block;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Method;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

class ResolverTest {

    private static final int SYSTEM_VARIABLE = 0;
    private static final int X_VARIABLE = 1;
    private static final int X2_VARIABLE = 2;
    private static final int TEST_VARIABLE = 2;
    private static final int THIS_VARIABLE = 5;
    private static final int STATIC_THIS_VARIABLE = 4;
    private static final int X_AFTER_TEST_VARIABLE = 3;
    public static final int TEST_BEFORE_X_VARIABLE = 1;
    public static final int STATIC_THIS_BEFORE_X_VARIABLE = 2;
    public static final int THIS_BEFORE_X_VARIABLE = 3;

    @Test
    void testVar() {
        testIncorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Var(1, "x", null)
                        )
                ),
                "[line 1] Error: The variable x is defined, but never used."
        );

        testIncorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Var(1, "x",
                                        new Expression.Variable(1, "x"))
                        )
                ),
                "[line 1] Error: The variable x cannot be used in its own initializer."
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
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new ResolvedStatement.Expression(2,
                                        new Variable(2, X_VARIABLE)
                                )
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
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new ResolvedStatement.Expression(1,
                                        new ResolvedExpression.Assign(1,
                                                X_VARIABLE,
                                                new ResolvedExpression.Literal(1, 1L)
                                        )
                                ),
                                new ResolvedStatement.Expression(2,
                                        new Variable(2, X_VARIABLE)
                                )
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
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Block(2,
                                        Map.of(X2_VARIABLE, "x"),
                                        new ResolvedStatement.Expression(2,
                                                new Variable(2,
                                                        X2_VARIABLE)
                                        )
                                ),
                                new ResolvedStatement.Expression(5,
                                        new Variable(5,
                                                X_VARIABLE)
                                )
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
                        new Block(0,
                                new ResolvedStatement.If(1,
                                        new ResolvedExpression.Literal(1, 1L),
                                        new ResolvedStatement.Expression(2,
                                                new ResolvedExpression.Literal(1, 1L)
                                        ),
                                        null
                                )
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
                        new Block(0,
                                new ResolvedStatement.Expression(1,
                                        new ResolvedExpression.Call(1,
                                                new Variable(1,
                                                        SYSTEM_VARIABLE),
                                                "print",
                                                new ResolvedExpression.Literal(1, 1L)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );
    }

    @Test
    void testClass() {
        testCorrect(
                new Statement.Block(0,
                        List.of(
                                new Statement.Class(1,
                                        "Test",
                                        emptyList(),
                                        emptyList(),
                                        List.of(
                                                new Statement.Method(2,
                                                        "test"
                                                )
                                        )
                                ),
                                new Statement.Expression(3,
                                        new Expression.Variable(3, "Test")
                                )
                        )
                ),
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_BEFORE_X_VARIABLE, "Test"),
                                Map.of(TEST_BEFORE_X_VARIABLE,
                                        new ResolvedStatement.Class(1,
                                                emptyList(),
                                                emptyList(),
                                                List.of(
                                                        new Method(2,
                                                                "test",
                                                                null,
                                                                new Block(2),
                                                                emptyMap()
                                                        )
                                                ),
                                                Map.of(THIS_BEFORE_X_VARIABLE, "this"),
                                                Map.of(STATIC_THIS_BEFORE_X_VARIABLE, "this"),
                                                THIS_BEFORE_X_VARIABLE, STATIC_THIS_BEFORE_X_VARIABLE
                                        )
                                ),
                                new ResolvedStatement.Expression(3,
                                        new Variable(3, TEST_BEFORE_X_VARIABLE)
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
                                                        "test"
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
                        new Block(0,
                                Map.of(TEST_BEFORE_X_VARIABLE, "Test"),
                                Map.of(TEST_BEFORE_X_VARIABLE,
                                        new ResolvedStatement.Class(1,
                                                List.of(
                                                        new Method(2,
                                                                "test",
                                                                null,
                                                                new Block(2),
                                                                emptyMap()
                                                        )
                                                ),
                                                emptyList(),
                                                emptyList(),
                                                Map.of(THIS_BEFORE_X_VARIABLE, "this"),
                                                Map.of(STATIC_THIS_BEFORE_X_VARIABLE, "this"),
                                                THIS_BEFORE_X_VARIABLE, STATIC_THIS_BEFORE_X_VARIABLE)
                                ),
                                new ResolvedStatement.Expression(3,
                                        new Variable(3, TEST_BEFORE_X_VARIABLE)
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
                        new Block(0,
                                Map.of(TEST_BEFORE_X_VARIABLE, "Test"),
                                Map.of(TEST_BEFORE_X_VARIABLE,
                                        new ResolvedStatement.Class(1,
                                                emptyList(),
                                                List.of(
                                                        new Method(2,
                                                                "test",
                                                                null,
                                                                new Block(2),
                                                                emptyMap()
                                                        )
                                                ),
                                                emptyList(),
                                                Map.of(THIS_BEFORE_X_VARIABLE, "this"),
                                                Map.of(STATIC_THIS_BEFORE_X_VARIABLE, "this"),
                                                THIS_BEFORE_X_VARIABLE, STATIC_THIS_BEFORE_X_VARIABLE)
                                ),
                                new ResolvedStatement.Expression(3,
                                        new Variable(3, TEST_BEFORE_X_VARIABLE)
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
        );
    }

    @Test
    void testFields() {
        testIncorrect(
                new Statement.Block(0,
                        new Statement.Expression(1,
                                new Expression.Field(1, "_hello")
                        )
                ),
                "[line 1] Error: Cannot reference a field '_hello' outside of a class definition."
        );

        testIncorrect(
                new Statement.Block(0,
                        new Statement.Expression(1,
                                new Expression.AssignField(1,
                                        "_hello",
                                        new Expression.Literal(1, "hello")
                                )
                        )
                ),
                "[line 1] Error: Cannot reference a field '_hello' outside of a class definition."
        );

        testIncorrect(
                new Statement.Block(0,
                        new Statement.Expression(1,
                                new Expression.StaticField(1, "__hello")
                        )
                ),
                "[line 1] Error: Cannot reference a field '__hello' outside of a class definition."
        );

        testIncorrect(
                new Statement.Block(0,
                        new Statement.Expression(1,
                                new Expression.AssignStaticField(1,
                                        "__hello",
                                        new Expression.Literal(1, "hello")
                                )
                        )
                ),
                "[line 1] Error: Cannot reference a field '__hello' outside of a class definition."
        );
    }

    @Test
    void testReturn() {
        testIncorrect(
                new Statement.Block(0,
                        new Statement.Return(1,
                                new Expression.Literal(1, "hello")
                        )
                ),
                "[line 1] Error: Cannot return from top-level code."
        );
    }

    @Test
    void testAssign() {
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

    @Test
    void testShadow() {
        testCorrect(
                new Statement.Block(0,
                        new Statement.Var(1, "x"),
                        new Statement.Block(2,
                                new Statement.Class(3,
                                        "Test",
                                        List.of(
                                                new Statement.Method(4,
                                                        "test",
                                                        new Statement.Expression(5,
                                                                new Expression.Variable(5,
                                                                        "x"
                                                                )
                                                        )
                                                )
                                        ),
                                        emptyList(),
                                        emptyList()
                                ),
                                new Statement.Var(6, "x"),
                                new Statement.Expression(7,
                                        new Expression.Variable(7,
                                                "Test"
                                        )
                                )
                        ),
                        new Statement.Expression(8,
                                new Expression.Variable(8,
                                        "x"
                                )
                        )
                ),
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Block(2,
                                        Map.of(TEST_VARIABLE, "Test", X_AFTER_TEST_VARIABLE, "x"),
                                        Map.of(
                                                TEST_VARIABLE,
                                                new ResolvedStatement.Class(3,
                                                        List.of(
                                                                new Method(4,
                                                                        "test",
                                                                        null,
                                                                        new Block(4,
                                                                                new ResolvedStatement.Expression(5,
                                                                                        new Variable(5,
                                                                                                X_AFTER_TEST_VARIABLE
                                                                                        )
                                                                                )
                                                                        ),
                                                                        emptyMap()
                                                                )
                                                        ),
                                                        emptyList(),
                                                        emptyList(),
                                                        Map.of(THIS_VARIABLE, "this"),
                                                        Map.of(STATIC_THIS_VARIABLE, "this"),
                                                        THIS_VARIABLE, STATIC_THIS_VARIABLE
                                                )
                                        ),
                                        new ResolvedStatement.Expression(7,
                                                new Variable(7, TEST_VARIABLE)
                                        )
                                ),
                                new ResolvedStatement.Expression(7,
                                        new Variable(7, X_VARIABLE)
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                )
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