package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.Collections;
import java.util.stream.IntStream;

public class InterpreterMethodUtils {
    static AbstractKnishObject.Method compileMethod(ResolvedStatement.Method method,
                                                    Environment enclosing,
                                                    Interpreter.InterpreterVisitor evaluator) {
        return arguments -> {
            // CHECK ARITY
            Environment withParameters = new Environment(enclosing,
                    method.argumentsIds == null ? Collections.emptyList() : method.argumentsIds);
            if (arguments != null) {
                assert method.argumentsIds != null;
                assert method.argumentsIds.size() == arguments.size();
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
