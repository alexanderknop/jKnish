package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishObject;
import org.github.alexanderknop.jknish.parser.Statement;

import java.util.stream.IntStream;

public class InterpreterMethodUtils {
    static AbstractKnishObject.Method compileMethod(KnishObject instance,
                                                    Statement.Method method,
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
            withParameters.define("this", instance);
            try {
                evaluator.interpret(withParameters, method.body);
            } catch (Return aReturn) {
                return aReturn.value;
            }
            return KnishCore.nil();
        };
    }
}
