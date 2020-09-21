package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishObject;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.Collections;
import java.util.stream.IntStream;

public class InterpreterMethodUtils {
    static AbstractKnishObject.Method compileMethod(KnishObject instance,
                                                    ResolvedStatement.Method method,
                                                    Environment enclosing,
                                                    Interpreter.InterpreterVisitor evaluator) {
        return arguments -> {
            Environment withParameters = new Environment(enclosing,
                    method.argumentsIds == null ? Collections.emptyList() : method.argumentsIds);
            if (arguments != null) {
                IntStream.range(0, arguments.size())
                        .forEach(i ->
                                withParameters.set(
                                        method.argumentsIds.get(i),
                                        arguments.get(i))
                        );
            }
            try {
                evaluator.interpret(withParameters, method.body);
            } catch (Return aReturn) {
                return aReturn.value;
            }
            return KnishCore.nil();
        };
    }
}
