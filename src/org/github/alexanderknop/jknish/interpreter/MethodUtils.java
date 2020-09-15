package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.parser.Statement;

import java.util.stream.IntStream;

public class MethodUtils {
    static AbstractKnishObject.Method compileMethod(Statement.Method method,
                                                    Environment enclosing,
                                                    Interpreter.InterpreterVisitor evaluator) {
        return arguments -> {
            Environment withParameters = new Environment(enclosing);
            if (arguments != null) {
                assert method.argumentsNames != null;
                IntStream.range(0, arguments.size())
                        .forEach(i ->
                                withParameters.define(
                                        method.argumentsNames.get(i),
                                        arguments.get(i))
                        );
            }
            evaluator.interpret(withParameters, method.body);
            return null;
        };
    }
}
