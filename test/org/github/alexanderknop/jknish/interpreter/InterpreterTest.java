package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.parser.LogicalOperator;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression.*;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Block;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Expression;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.Method;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.github.alexanderknop.jknish.resolver.ResolvedStatement.If;
import static org.github.alexanderknop.jknish.resolver.ResolvedStatement.While;
import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {
    private static final int SYSTEM_VARIABLE = 0;
    private static final int X_VARIABLE = 1;
    private static final int TEST_VARIABLE = 1;
    private static final int THIS_VARIABLE = 2;
    private static final int STATIC_THIS_VARIABLE = 3;
    private static final int X2_VARIABLE = 2;

    @Test
    void testPrint() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Literal(1, 1L)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "1"
        );
        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print"
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 1] Error: System metaclass does not implement 'print'."
        );
        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Literal(1, 1L),
                                                new Literal(1, 1L)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 1] Error: System metaclass does not implement 'print(_, _)'."
        );
    }

    @Test
    void testClassStaticMethods() {
        ResolvedStatement.Class testClass1 = new ResolvedStatement.Class(1,
                Map.of(
                        new MethodId("test", null),
                        new Method(2,
                                null,
                                new Block(2,
                                        new Expression(2,
                                                new Call(2,
                                                        new Variable(2,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Literal(2,
                                                                "Hello World!")
                                                )
                                        )
                                ),
                                emptyMap()
                        )
                ),
                emptyMap(),
                emptyMap(),
                Map.of(THIS_VARIABLE, "this"),
                Map.of(STATIC_THIS_VARIABLE, "this"),
                THIS_VARIABLE, STATIC_THIS_VARIABLE);
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass1
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4,
                                                        TEST_VARIABLE),
                                                "test"
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Hello World!"
        );

        ResolvedStatement.Class testClass2 = new ResolvedStatement.Class(1,
                emptyMap(),
                emptyMap(),
                emptyMap(),
                Map.of(THIS_VARIABLE, "this"),
                Map.of(STATIC_THIS_VARIABLE, "this"),
                THIS_VARIABLE, STATIC_THIS_VARIABLE);
        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass2
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4,
                                                        TEST_VARIABLE),
                                                "test"
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 4] Error: Test metaclass does not implement 'test'."
        );
    }

    @Test
    void testClassMethods() {

        ResolvedStatement.Class testClass1 = new ResolvedStatement.Class(1,
                emptyMap(),
                Map.of(
                        new MethodId("new", 0),
                        new Method(3,
                                emptyList(),
                                new Block(3),
                                emptyMap()
                        )
                ),
                Map.of(
                        new MethodId("test", null),
                        new Method(2,
                                null,
                                new Block(2,
                                        new Expression(2,
                                                new Call(2,
                                                        new Variable(2,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Literal(2,
                                                                "Hello World!")
                                                )
                                        )
                                ),
                                emptyMap()
                        )
                ),
                Map.of(THIS_VARIABLE, "this"),
                Map.of(STATIC_THIS_VARIABLE, "this"),
                THIS_VARIABLE, STATIC_THIS_VARIABLE);
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass1
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Call(4,
                                                        new Variable(4,
                                                                TEST_VARIABLE),
                                                        "new",
                                                        emptyList()
                                                ),
                                                "test"
                                        )
                                )

                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Hello World!"
        );
        ResolvedStatement.Class testClass2 = new ResolvedStatement.Class(1,
                emptyMap(),
                Map.of(
                        new MethodId("new", 0),
                        new Method(3,
                                emptyList(),
                                new Block(3),
                                emptyMap()
                        )
                ),
                emptyMap(),
                Map.of(THIS_VARIABLE, "this"),
                Map.of(STATIC_THIS_VARIABLE, "this"),
                THIS_VARIABLE, STATIC_THIS_VARIABLE);
        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass2
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Call(4,
                                                        new Variable(4,
                                                                TEST_VARIABLE),
                                                        "new",
                                                        emptyList()
                                                ),
                                                "test"
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 4] Error: Test does not implement 'test'."
        );
    }

    @Test
    void testReturn() {
        ResolvedStatement.Class testClass1 = new ResolvedStatement.Class(1,
                Map.of(
                        new MethodId("test", null),
                        new Method(2,
                                null,
                                new Block(2,
                                        new ResolvedStatement.Return(2,
                                                new Literal(2,
                                                        "Hello World!")
                                        )
                                ),
                                emptyMap()
                        )
                ),
                emptyMap(),
                emptyMap(),
                Map.of(THIS_VARIABLE, "this"),
                Map.of(STATIC_THIS_VARIABLE, "this"),
                THIS_VARIABLE, STATIC_THIS_VARIABLE);
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass1
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(4,
                                                        new Variable(4,
                                                                TEST_VARIABLE),
                                                        "test"
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Hello World!"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test",
                                        X2_VARIABLE, "x"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass1
                                ),
                                new Expression(3,
                                        new Assign(3, X2_VARIABLE,
                                                new Literal(3, "Hello World!")
                                        )
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(4,
                                                        new Variable(4,
                                                                TEST_VARIABLE),
                                                        "test"
                                                )
                                        )
                                ),
                                new Expression(5,
                                        new Call(5,
                                                new Variable(5, SYSTEM_VARIABLE),
                                                "print",
                                                new Variable(5, X2_VARIABLE)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Hello World!\nHello World!"
        );
    }

    @Test
    void testThis() {
        ResolvedStatement.Class testClass1 =
                new ResolvedStatement.Class(1,
                        Map.of(
                                new MethodId("test1", null),
                                new Method(2,
                                        null,
                                        new Block(2,
                                                new Expression(3,
                                                        new Call(3,
                                                                new Variable(3,
                                                                        SYSTEM_VARIABLE),
                                                                "print",
                                                                new Literal(3,
                                                                        "Hello World!")
                                                        )
                                                )
                                        ),
                                        emptyMap()
                                ),
                                new MethodId("test2", null),
                                new Method(3,
                                        null,
                                        new Block(3,
                                                new Expression(3,
                                                        new Call(3,
                                                                new Variable(3,
                                                                        STATIC_THIS_VARIABLE),
                                                                "test1"
                                                        )
                                                )
                                        ),
                                        emptyMap()
                                )
                        ),
                        emptyMap(),
                        emptyMap(),
                        Map.of(THIS_VARIABLE, "this"),
                        Map.of(STATIC_THIS_VARIABLE, "this"),
                        THIS_VARIABLE, STATIC_THIS_VARIABLE
                );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass1
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4,
                                                        TEST_VARIABLE),
                                                "test2"
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Hello World!"
        );

        ResolvedStatement.Class testClass2 =
                new ResolvedStatement.Class(1,
                        Map.of(
                                new MethodId("test1", null),
                                new Method(2,
                                        null,
                                        new Block(2,
                                                new Expression(3,
                                                        new Call(3,
                                                                new Variable(3,
                                                                        SYSTEM_VARIABLE),
                                                                "print",
                                                                new Literal(3,
                                                                        "Hello World!")
                                                        )
                                                )
                                        ),
                                        emptyMap()
                                ),
                                new MethodId("test2", null),
                                new Method(3,
                                        null,
                                        new Block(3,
                                                new Expression(3,
                                                        new Call(3,
                                                                new Variable(3,
                                                                        STATIC_THIS_VARIABLE),
                                                                "test"
                                                        )
                                                )
                                        ),
                                        emptyMap()
                                )
                        ),
                        emptyMap(),
                        emptyMap(),
                        Map.of(THIS_VARIABLE, "this"),
                        Map.of(STATIC_THIS_VARIABLE, "this"),
                        THIS_VARIABLE, STATIC_THIS_VARIABLE
                );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass2
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4,
                                                        TEST_VARIABLE),
                                                "test2"
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 3] Error: Test metaclass does not implement 'test'."
        );
    }

    @Test
    void testConstructor() {
        ResolvedStatement.Class testClass1 =
                new ResolvedStatement.Class(1,
                        emptyMap(),
                        Map.of(
                                new MethodId("new", 0),
                                new Method(2,
                                        emptyList(),
                                        new Block(2,
                                                new Expression(3,
                                                        new Call(3,
                                                                new Variable(3,
                                                                        THIS_VARIABLE),
                                                                "test"
                                                        )
                                                )
                                        ),
                                        emptyMap()
                                )
                        ),
                        Map.of(
                                new MethodId("test", null),
                                new Method(2,
                                        null,
                                        new Block(2,
                                                new Expression(3,
                                                        new Call(3,
                                                                new Variable(3,
                                                                        SYSTEM_VARIABLE),
                                                                "print",
                                                                new Literal(3,
                                                                        "Hello World!")
                                                        )
                                                )
                                        ),
                                        emptyMap()
                                )
                        ),
                        Map.of(THIS_VARIABLE, "this"),
                        Map.of(STATIC_THIS_VARIABLE, "this"),
                        THIS_VARIABLE, STATIC_THIS_VARIABLE
                );
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass1
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4,
                                                        TEST_VARIABLE),
                                                "new",
                                                emptyList()
                                        )
                                )

                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Hello World!"
        );

        ResolvedStatement.Class testClass2 =
                new ResolvedStatement.Class(1,
                        emptyMap(),
                        Map.of(
                                new MethodId("new1", 0),
                                new Method(4,
                                        emptyList(),
                                        new Block(4,
                                                new Expression(5,
                                                        new Call(5,
                                                                new Variable(5,
                                                                        SYSTEM_VARIABLE),
                                                                "print",
                                                                new Literal(5,
                                                                        "Hello World 1!"
                                                                )
                                                        )
                                                )
                                        ),
                                        emptyMap()
                                ),
                                new MethodId("new2", 0),
                                new Method(4,
                                        emptyList(),
                                        new Block(4,
                                                new Expression(5,
                                                        new Call(5,
                                                                new Variable(5,
                                                                        SYSTEM_VARIABLE),
                                                                "print",
                                                                new Literal(5,
                                                                        "Hello World 2!"
                                                                )
                                                        )
                                                )
                                        ),
                                        emptyMap()
                                )
                        ),
                        emptyMap(),
                        Map.of(THIS_VARIABLE, "this"),
                        Map.of(STATIC_THIS_VARIABLE, "this"),
                        THIS_VARIABLE, STATIC_THIS_VARIABLE
                );
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass2
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4,
                                                        TEST_VARIABLE),
                                                "new1",
                                                emptyList()
                                        )
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4,
                                                        TEST_VARIABLE),
                                                "new2",
                                                emptyList()
                                        )
                                )

                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Hello World 1!\nHello World 2!"
        );
    }

    @Test
    void testAddition() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(1,
                                                        new Literal(1, 1L),
                                                        "+",
                                                        new Literal(1, 2L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "3"
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Literal(1, Boolean.TRUE),
                                                "+",
                                                new Literal(1, 1L)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 1] Error: Boolean does not implement '+(_)'."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Literal(1, 1L),
                                                "+",
                                                new Literal(1, Boolean.TRUE)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 1] Error: Right operand must be a number."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Literal(1, 1L),
                                                "+"
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 1] Error: Number does not implement '+'."
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(1,
                                                        new Literal(1, 1L),
                                                        "-",
                                                        new Literal(1, 2L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "-1"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(1,
                                                        new Literal(1, 1L),
                                                        "-"
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "-1"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(1,
                                                        new Literal(1, "Hello "),
                                                        "+",
                                                        new Literal(1, "World!")
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Hello World!"
        );
    }

    @Test
    void testMultiplication() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(1,
                                                        new Literal(1, 3L),
                                                        "*",
                                                        new Literal(1, 2L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "6"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(1,
                                                        new Literal(1, 6L),
                                                        "/",
                                                        new Literal(1, 2L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "3"
        );


        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(1,
                                                        new Literal(1, Boolean.TRUE),
                                                        "*",
                                                        new Literal(1, 2L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 1] Error: Boolean does not implement '*(_)'."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Call(1,
                                                        new Literal(1, 2L),
                                                        "*",
                                                        new Literal(1, Boolean.TRUE)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 1] Error: Right operand must be a number."
        );
    }

    @Test
    void testVar() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1, X_VARIABLE,
                                                new Literal(1, 1L))
                                ),
                                new Expression(2,
                                        new Call(2,
                                                new Variable(2, SYSTEM_VARIABLE),
                                                "print",
                                                new Variable(2, X_VARIABLE)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "1"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1, X_VARIABLE,
                                                new Literal(1, 1L))
                                ),
                                new Block(2,
                                        Map.of(X2_VARIABLE, "x"),
                                        new Expression(3,
                                                new Assign(3,
                                                        X2_VARIABLE,
                                                        new Literal(3, 2L)
                                                )
                                        ),
                                        new Expression(4,
                                                new Call(4,
                                                        new Variable(4,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Variable(4,
                                                                X2_VARIABLE)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "2"
        );
    }

    @Test
    void testAssign() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 2L)
                                        )
                                ),
                                new Expression(3,
                                        new Call(3,
                                                new Variable(3, SYSTEM_VARIABLE),
                                                "print",
                                                new Variable(3, X_VARIABLE)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "2"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Block(2,
                                        Map.of(X2_VARIABLE, "x"),
                                        new Expression(2,
                                                new Assign(2,
                                                        X2_VARIABLE,
                                                        new Literal(2, 2L)
                                                )
                                        ),
                                        new Expression(3,
                                                new Call(3,
                                                        new Variable(3,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Variable(3,
                                                                X2_VARIABLE)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "2"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Block(2,
                                        Map.of(X2_VARIABLE, "x"),
                                        new Expression(2,
                                                new Assign(2,
                                                        X2_VARIABLE,
                                                        new Literal(2, 2L)
                                                )
                                        )
                                ),
                                new Expression(3,
                                        new Call(3,
                                                new Variable(3,
                                                        SYSTEM_VARIABLE),
                                                "print",
                                                new Variable(3,
                                                        X_VARIABLE)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "1"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Expression(2,
                                        new Assign(2,
                                                X_VARIABLE,
                                                new Call(2,
                                                        new Variable(2, X_VARIABLE),
                                                        "+",
                                                        new Literal(2, 2L)
                                                )
                                        )
                                ),
                                new Expression(3,
                                        new Call(3,
                                                new Variable(3,
                                                        SYSTEM_VARIABLE),
                                                "print",
                                                new Variable(3,
                                                        X_VARIABLE)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "3"
        );

    }

    @Test
    void testIf() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new If(1,
                                        new Literal(1, Boolean.FALSE),
                                        new Expression(2,
                                                new Call(2,
                                                        new Variable(2,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Literal(2, 1L)
                                                )
                                        ),
                                        new Expression(3,
                                                new Call(3,
                                                        new Variable(3,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Literal(3, 2L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "2"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new If(1,
                                        new Literal(1, Boolean.TRUE),
                                        new Expression(2,
                                                new Call(2,
                                                        new Variable(2,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Literal(2, 1L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "1"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new If(1,
                                        new Literal(1, Boolean.FALSE),
                                        new Expression(2,
                                                new Call(2,
                                                        new Variable(2,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Literal(2, 1L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                ""
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new If(1,
                                        new Literal(1, 1L),
                                        new Expression(2,
                                                new Call(2,
                                                        new Variable(2,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Literal(2, 1L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 1] Error: Condition must have type Boolean."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new If(1,
                                        new Literal(1, null),
                                        new Expression(2,
                                                new Call(2,
                                                        new Variable(2,
                                                                SYSTEM_VARIABLE),
                                                        "print",
                                                        new Literal(2, 1L)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 1] Error: If condition cannot be nil."
        );
    }

    @Test
    void testWhile() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 0L)
                                        )
                                ),
                                new While(2,
                                        new Call(2,
                                                new Variable(2, X_VARIABLE),
                                                "<",
                                                new Literal(2, 3L)),
                                        new Block(3,
                                                new Expression(4,
                                                        new Call(4,
                                                                new Variable(4,
                                                                        SYSTEM_VARIABLE
                                                                ),
                                                                "print",
                                                                new Variable(4,
                                                                        X_VARIABLE
                                                                )
                                                        )
                                                ),
                                                new Expression(5,
                                                        new Assign(5,
                                                                X_VARIABLE,
                                                                new Call(5,
                                                                        new Variable(5,
                                                                                X_VARIABLE),
                                                                        "+",
                                                                        new Literal(5,
                                                                                1L)
                                                                )
                                                        )
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "0\n1\n2\n"
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 0L)
                                        )
                                ),
                                new While(2,
                                        new Variable(2, X_VARIABLE),
                                        new Block(3)
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 2] Error: Condition must have type Boolean."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new While(2,
                                        new Variable(2, X_VARIABLE),
                                        new Block(3)
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 2] Error: While condition cannot be nil."
        );
    }

    @Test
    void testLogical() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Logical(1,
                                                        new Literal(1, Boolean.TRUE),
                                                        LogicalOperator.AND,
                                                        new Literal(1, Boolean.FALSE)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "false"
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Variable(1, SYSTEM_VARIABLE),
                                                "print",
                                                new Logical(1,
                                                        new Literal(1, Boolean.TRUE),
                                                        LogicalOperator.OR,
                                                        new Literal(1, Boolean.FALSE)
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "true"
        );

        ResolvedStatement.Class testClass = new ResolvedStatement.Class(1,
                Map.of(
                        new MethodId("test", null),
                        new Method(2,
                                null,
                                new Block(2,
                                        new Expression(1,
                                                new Call(1,
                                                        new Variable(1, SYSTEM_VARIABLE),
                                                        "print",
                                                        new Literal(1,
                                                                "Right operand is computed!")
                                                )
                                        ),
                                        new ResolvedStatement.Return(2,
                                                new Literal(2,
                                                        Boolean.TRUE)
                                        )
                                ),
                                emptyMap()
                        )
                ),
                emptyMap(),
                emptyMap(),
                Map.of(THIS_VARIABLE, "this"),
                Map.of(STATIC_THIS_VARIABLE, "this"),
                THIS_VARIABLE, STATIC_THIS_VARIABLE);
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass
                                ),
                                new Expression(3,
                                        new Logical(3,
                                                new Literal(3, Boolean.TRUE),
                                                LogicalOperator.OR,
                                                new Call(3,
                                                        new Variable(3,
                                                                TEST_VARIABLE
                                                        ),
                                                        "test"
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                ""
        );
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass
                                ),
                                new Expression(3,
                                        new Logical(3,
                                                new Literal(3, Boolean.FALSE),
                                                LogicalOperator.AND,
                                                new Call(3,
                                                        new Variable(3,
                                                                TEST_VARIABLE
                                                        ),
                                                        "test"
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                ""
        );
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass
                                ),
                                new Expression(3,
                                        new Logical(3,
                                                new Literal(3, Boolean.FALSE),
                                                LogicalOperator.OR,
                                                new Call(3,
                                                        new Variable(3,
                                                                TEST_VARIABLE
                                                        ),
                                                        "test"
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Right operand is computed!"
        );
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        testClass
                                ),
                                new Expression(3,
                                        new Logical(3,
                                                new Literal(3, Boolean.TRUE),
                                                LogicalOperator.AND,
                                                new Call(3,
                                                        new Variable(3,
                                                                TEST_VARIABLE
                                                        ),
                                                        "test"
                                                )
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "Right operand is computed!"
        );
    }

    void testCorrect(ResolvedScript script, String expectedOutput) {
        StringWriter errorWriter = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(errorWriter);

        StringWriter outputWriter = new StringWriter();
        KnishCore core = new KnishCore(outputWriter);


        Interpreter.interpret(core, script, reporter);

        assertFalse(reporter.hadError(), "The script is correct;" +
                " the error message is:\n" + errorWriter.toString());
        String actual = outputWriter.toString().strip();
        assertEquals(expectedOutput.strip(), actual,
                "The output is supposed to be '" +
                        expectedOutput.strip() + "' instead of '" +
                        actual + "'.");
    }

    void testIncorrect(ResolvedScript script, String expectedError) {
        StringWriter error = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(error);
        KnishCore core = new KnishCore(new StringWriter());

        Interpreter.interpret(core, script, reporter);

        assertTrue(reporter.hadError(), "The script is incorrect.");
        String actual = error.toString().strip();
        assertEquals(expectedError, actual,
                "The error message is supposed to be '" +
                        expectedError.strip() + "' instead of '" +
                        actual + "'.");
    }
}