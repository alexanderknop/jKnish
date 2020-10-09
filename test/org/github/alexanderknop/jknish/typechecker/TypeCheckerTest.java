package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishStandardModule;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression.Assign;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression.Call;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression.Literal;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression.Variable;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement.*;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

class TypeCheckerTest {

    private static final int SYSTEM_VARIABLE = 0;
    private static final int X_VARIABLE = 1;
    private static final int TEST_VARIABLE = 1;
    private static final int THIS_VARIABLE = 2;
    private static final int STATIC_THIS_VARIABLE = 3;
    private static final int X_ARGUMENT_VARIABLE = 4;

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
                )
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
                "[line 1] Error: System metaclass does not implement print."
        );
    }

    @Test
    void testClassStaticMethods() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        new ResolvedStatement.Class(1,
                                                Map.of(
                                                        new MethodId("test", null),
                                                        new Method(2,
                                                                null,
                                                                new Block(2),
                                                                emptyMap()
                                                        )
                                                ),
                                                emptyMap(),
                                                emptyMap(),
                                                Map.of(THIS_VARIABLE, "this"),
                                                Map.of(STATIC_THIS_VARIABLE, "this"),
                                                THIS_VARIABLE, STATIC_THIS_VARIABLE)
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4, TEST_VARIABLE),
                                                "test"
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        new ResolvedStatement.Class(1,
                                                emptyMap(),
                                                emptyMap(),
                                                emptyMap(),
                                                Map.of(THIS_VARIABLE, "this"),
                                                Map.of(STATIC_THIS_VARIABLE, "this"),
                                                THIS_VARIABLE, STATIC_THIS_VARIABLE)
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4, TEST_VARIABLE),
                                                "test"
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 4] Error: Test metaclass does not implement test."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(
                                        TEST_VARIABLE,
                                        new ResolvedStatement.Class(1,
                                                emptyMap(),
                                                emptyMap(),
                                                emptyMap(),
                                                Map.of(THIS_VARIABLE, "this"),
                                                Map.of(STATIC_THIS_VARIABLE, "this"),
                                                THIS_VARIABLE, STATIC_THIS_VARIABLE)
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4, TEST_VARIABLE),
                                                "test"
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 4] Error: Test metaclass does not implement test."
        );
    }

    @Test
    void testClassConstructors() {
        ResolvedStatement.Class testClass = new ResolvedStatement.Class(1,
                emptyMap(),
                Map.of(
                        new MethodId("new", 0),
                        new Method(2,
                                emptyList(),
                                new Block(2),
                                emptyMap()
                        )
                ),
                emptyMap(),
                Map.of(THIS_VARIABLE, "this"),
                Map.of(STATIC_THIS_VARIABLE, "this"),
                THIS_VARIABLE, STATIC_THIS_VARIABLE);
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(TEST_VARIABLE, "Test"),
                                Map.of(TEST_VARIABLE,
                                        testClass
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4, TEST_VARIABLE),
                                                "new",
                                                emptyList()
                                        )
                                )
                        ),
                        emptyMap()
                )
        );
    }

    @Test
    void testClassMethods() {
        ResolvedStatement.Class testClass1 = new ResolvedStatement.Class(1,
                emptyMap(),
                Map.of(
                        new MethodId("new", 0),
                        new Method(2,
                                emptyList(),
                                new Block(2),
                                emptyMap()
                        )
                ),
                Map.of(
                        new MethodId("test", null),
                        new Method(2,
                                null,
                                new Block(2),
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
                                                        new Variable(4, TEST_VARIABLE),
                                                        "new",
                                                        emptyList()
                                                ),
                                                "test"
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        ResolvedStatement.Class testClass2 = new ResolvedStatement.Class(1,
                emptyMap(),
                Map.of(
                        new MethodId("new", 0),
                        new Method(2,
                                emptyList(),
                                new Block(2),
                                emptyMap()
                        )
                ),
                Map.of(
                        new MethodId("test", 1),
                        new Method(2,
                                List.of(
                                        X_ARGUMENT_VARIABLE
                                ),
                                new Block(2,
                                        new Expression(3,
                                                new Call(3,
                                                        new Variable(3,
                                                                X_ARGUMENT_VARIABLE),
                                                        "+",
                                                        new Literal(3, 1L)
                                                )
                                        )
                                ),
                                Map.of(X_ARGUMENT_VARIABLE, "x")
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
                                                "test",
                                                new Literal(4, 1L)
                                        )
                                )
                        ),
                        emptyMap()
                )
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
                                                new Call(4,
                                                        new Variable(4,
                                                                TEST_VARIABLE),
                                                        "new",
                                                        emptyList()
                                                ),
                                                "test",
                                                new Literal(4, "Hello")
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 4] Error: The value of 0th argument of test(_) has incompatible type."
        );

        ResolvedStatement.Class testClass3 = new ResolvedStatement.Class(1,
                emptyMap(),
                Map.of(
                        new MethodId("new", 0),
                        new Method(2,
                                emptyList(),
                                new Block(2),
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
                                        testClass3
                                ),
                                new Expression(4,
                                        new Call(4,
                                                new Variable(4, TEST_VARIABLE),
                                                "test"
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 4] Error: Test metaclass does not implement test."
        );
    }

    @Test
    void testThis() {
        ResolvedStatement.Class testClass1 = new ResolvedStatement.Class(1,
                emptyMap(),
                Map.of(
                        new MethodId("new", 0),
                        new Method(2,
                                emptyList(),
                                new Block(2),
                                emptyMap()
                        )
                ),
                Map.of(
                        new MethodId("test1", null),
                        new Method(3,
                                null,
                                new Block(3),
                                emptyMap()
                        ),
                        new MethodId("test2", null),
                        new Method(4,
                                null,
                                new Block(4,
                                        new Expression(5,
                                                new Call(5,
                                                        new Variable(5,
                                                                THIS_VARIABLE
                                                        ),
                                                        "test1"
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
                                new Expression(6,
                                        new Call(6,
                                                new Call(6,
                                                        new Variable(4, TEST_VARIABLE),
                                                        "new",
                                                        emptyList()
                                                ),
                                                "test2"
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        ResolvedStatement.Class testClass2 = new ResolvedStatement.Class(1,
                emptyMap(),
                Map.of(
                        new MethodId("new", 0),
                        new Method(2,
                                emptyList(),
                                new Block(2),
                                emptyMap()
                        )
                ),
                Map.of(
                        new MethodId("test1", null),
                        new Method(3,
                                null,
                                new Block(3),
                                emptyMap()
                        ),
                        new MethodId("test2", null),
                        new Method(4,
                                null,
                                new Block(4,
                                        new Expression(5,
                                                new Call(5,
                                                        new Variable(5,
                                                                THIS_VARIABLE
                                                        ),
                                                        "test"
                                                )
                                        )
                                ),
                                emptyMap()
                        )
                ),
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
                                new Expression(6,
                                        new Call(6,
                                                new Call(6,
                                                        new Variable(6,
                                                                TEST_VARIABLE
                                                        ),
                                                        "new",
                                                        emptyList()
                                                ),
                                                "test2"
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 5] Error: Test does not implement test."
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
                                        new Return(3,
                                                new Literal(3,
                                                        "Hello World")
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
                )
        );


        ResolvedStatement.Class testClass2 = new ResolvedStatement.Class(1,
                Map.of(
                        new MethodId("test", null),
                        new Method(2,
                                null,
                                new Block(2,
                                        new If(3,
                                                new Literal(3, Boolean.TRUE),
                                                new Return(3,
                                                        new Literal(3,
                                                                1L)
                                                ),
                                                new Return(3,
                                                        new Literal(3,
                                                                "Hello World")
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
                                        testClass2
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
                )
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
                                                new Call(4,
                                                        new Variable(4,
                                                                TEST_VARIABLE),
                                                        "test"
                                                ),
                                                "+",
                                                new Literal(4, 1L)
                                        )
                                )
                        ),
                        Map.of(SYSTEM_VARIABLE, "System")
                ),
                "[line 4] Error: The value of 0th argument of +(_) has incompatible type."
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
                                                new Literal(4, 1L),
                                                "+",
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
                "[line 4] Error: The value of 0th argument of +(_) has incompatible type."
        );

        ResolvedStatement.Class testClass3 = new ResolvedStatement.Class(1,
                Map.of(
                        new MethodId("test", null),
                        new Method(2,
                                null,
                                new Block(2,
                                        new If(3,
                                                new Literal(3, Boolean.TRUE),
                                                new Return(3,
                                                        new Literal(3,
                                                                1L)
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
                                        testClass3
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
                )
        );
    }

    @Test
    void testAddition() {
        testCorrect(new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Literal(1, 1L),
                                                "+",
                                                new Literal(1, 1L)
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Literal(1, 1L),
                                                "+",
                                                new Literal(1,
                                                        Boolean.TRUE)
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 1] Error: The value of 0th argument of +(_) has incompatible type."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Literal(1,
                                                        Boolean.TRUE),
                                                "+",
                                                new Literal(1, 1L)
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 1] Error: An object does not implement +(_)."
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Literal(1,
                                                        "Hello "),
                                                "+",
                                                new Literal(1,
                                                        "World")
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                new Expression(1,
                                        new Call(1,
                                                new Literal(1,
                                                        "Hello "),
                                                "+",
                                                new Literal(1, 1L)
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 1] Error: The value of 0th argument of +(_) has incompatible type."
        );
    }

    @Test
    void testVar() {
        int xVariable = 1;
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                xVariable,
                                                new Literal(1, 1L))
                                ),
                                new Expression(2,
                                        new Call(2,
                                                new Variable(2,
                                                        xVariable),
                                                "+",
                                                new Literal(2, 1L)
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(2,
                                        new Call(2,
                                                new Variable(2,
                                                        xVariable),
                                                "+",
                                                new Literal(2, 1L)
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

//        testCorrect(
//                new ResolvedScript(
//                        new Block(0,
//                                Map.of(xVariable, "x"),
//                                new Expression(2,
//                                        new Call(2,
//                                                new Variable(2,
//                                                        SYSTEM_VARIABLE),
//                                                "print",
//                                                new Variable(2,
//                                                        xVariable)
//                                        )
//                                )
//                        ),
//                        Map.of(SYSTEM_VARIABLE, "System")
//                )
//        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(2,
                                        new Call(2,
                                                new Variable(2,
                                                        xVariable),
                                                "+",
                                                new Literal(2,
                                                        "y")
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                xVariable,
                                                new Literal(1, 1L))
                                ),
                                new Expression(2,
                                        new Call(2,
                                                new Variable(2,
                                                        xVariable),
                                                "+",
                                                new Literal(2, "s")
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 2] Error: The value of 0th argument of +(_) has incompatible type.");
    }

    @Test
    void testAssign() {
        int xVariable = 1;
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                xVariable,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Expression(2,
                                        new Assign(2,
                                                xVariable,
                                                new Literal(2, 2L)
                                        )
                                ),
                                new Expression(2,
                                        new Call(2,
                                                new Variable(2,
                                                        xVariable),
                                                "+",
                                                new Literal(2, 1L)
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                xVariable,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Expression(3,
                                        new Assign(3,
                                                xVariable,
                                                new Call(3,
                                                        new Variable(3,
                                                                xVariable),
                                                        "+",
                                                        new Literal(3,
                                                                1L)
                                                )
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                xVariable,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Expression(2,
                                        new Assign(2,
                                                xVariable,
                                                new Literal(2, "y")
                                        )
                                )
                        ),
                        emptyMap()
                )
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                xVariable,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Expression(2,
                                        new Assign(2,
                                                xVariable,
                                                new Literal(2, "y")
                                        )
                                ),
                                new Expression(3,
                                        new Call(3,
                                                new Variable(3, xVariable),
                                                "+",
                                                new Literal(3, 1L)
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 3] Error: The value of 0th argument of +(_) has incompatible type."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                xVariable,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Expression(2,
                                        new Assign(2,
                                                xVariable,
                                                new Literal(2, "y")
                                        )
                                ),
                                new Expression(3,
                                        new Call(3,
                                                new Variable(3, xVariable),
                                                "+",
                                                new Literal(3, "y")
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 3] Error: The value of 0th argument of +(_) has incompatible type."
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(xVariable, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                xVariable,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new Expression(2,
                                        new Assign(2,
                                                xVariable,
                                                new Literal(2, Boolean.TRUE)
                                        )
                                ),
                                new Expression(3,
                                        new Call(3,
                                                new Variable(3, xVariable),
                                                "+",
                                                new Literal(3, 1L)
                                        )
                                )
                        ),
                        emptyMap()
                ),
                "[line 3] Error: An object referred by the variable 'x' does not implement +(_)."
        );

//        testCorrect(
//                new ResolvedScript(
//                        new Block(0,
//                                Map.of(xVariable, "x"),
//                                new Expression(1,
//                                        new Assign(1,
//                                                xVariable,
//                                                new Literal(1, 1L)
//                                        )
//                                ),
//                                new Expression(2,
//                                        new Assign(2,
//                                                xVariable,
//                                                new Literal(2, "y")
//                                        )
//                                ),
//                                new Expression(3,
//                                        new Call(1,
//                                                new Variable(1, SYSTEM_VARIABLE),
//                                                "print",
//                                                new Variable(1, xVariable)
//                                        )
//                                )
//                        ),
//                        Map.of(SYSTEM_VARIABLE, "System")
//                )
//        );
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
                                                new Literal(1, Boolean.TRUE)
                                        )
                                ),
                                new While(2,
                                        new Variable(2, X_VARIABLE),
                                        new Expression(3, new Literal(3, 1L))
                                )
                        ),
                        emptyMap()
                )
        );
        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new While(2,
                                        new Variable(2, X_VARIABLE),
                                        new Expression(3, new Literal(3, 1L))
                                )
                        ),
                        emptyMap()
                ),
                "[line 2] Error: While conditions must have type Boolean."
        );
    }

    @Test
    void testIf() {
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, Boolean.TRUE)
                                        )
                                ),
                                new If(2,
                                        new Variable(2, X_VARIABLE),
                                        new Expression(3, new Literal(3, 1L)),
                                        new Expression(4, new Literal(3, 1L))
                                )
                        ),
                        emptyMap()
                )
        );
        testCorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, Boolean.TRUE)
                                        )
                                ),
                                new If(2,
                                        new Variable(2, X_VARIABLE),
                                        new Expression(3, new Literal(3, 1L))
                                )
                        ),
                        emptyMap()
                )
        );

        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new If(2,
                                        new Variable(2, X_VARIABLE),
                                        new Expression(3, new Literal(3, 1L)),
                                        new Expression(4, new Literal(3, 1L))
                                )
                        ),
                        emptyMap()
                ),
                "[line 2] Error: If conditions must have type Boolean."
        );
        testIncorrect(
                new ResolvedScript(
                        new Block(0,
                                Map.of(X_VARIABLE, "x"),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, Boolean.TRUE)
                                        )
                                ),
                                new Expression(1,
                                        new Assign(1,
                                                X_VARIABLE,
                                                new Literal(1, 1L)
                                        )
                                ),
                                new If(2,
                                        new Variable(2, X_VARIABLE),
                                        new Expression(3, new Literal(3, 1L)),
                                        new Expression(4, new Literal(3, 1L))
                                )
                        ),
                        emptyMap()
                ),
                "[line 2] Error: If conditions must have type Boolean."
        );
    }

    @Test
    void testPointerEquality() {
        Object[] literals = {
                null,
                Boolean.TRUE,
                1L,
                "hello"
        };
        String[] operators = {"===", "!=="};

        for (Object literal : literals) {
            for (String operator : operators) {
                testCorrect(
                        new ResolvedScript(
                                new Block(0,
                                        Map.of(X_VARIABLE, "x"),
                                        new Expression(1,
                                                new Assign(1,
                                                        X_VARIABLE,
                                                        new Literal(1, literal)
                                                )
                                        ),
                                        new Expression(2,
                                                new Call(2,
                                                        new Variable(2, X_VARIABLE),
                                                        operator,
                                                        new Variable(2, X_VARIABLE)
                                                )
                                        )
                                ),
                                emptyMap()
                        )
                );
            }
        }
    }


    private void testCorrect(ResolvedScript script) {
        Writer errors = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(errors);
        TypeChecker.check(script, reporter, new KnishStandardModule(new StringWriter()));
        assertFalse(reporter.hadError(), "The script is supposed to be correct:\n" +
                errors);
    }

    private void testIncorrect(ResolvedScript script, String expectedMessage) {
        Writer errors = new StringWriter();
        KnishErrorReporter reporter = new KnishErrorReporter(errors);
        TypeChecker.check(script, reporter, new KnishStandardModule(new StringWriter()));
        assertTrue(reporter.hadError(), "The script is supposed to be incorrect");
        assertEquals(expectedMessage.strip(), errors.toString().strip(),
                "The error message is expected to be:\n" +
                        expectedMessage + "\ninstead of\n" + errors);
    }
}