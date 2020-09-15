package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.parser.Statement;

import java.util.stream.IntStream;

class KnishClassInstance extends AbstractKnishObject {
    private final Statement.Class klass;

    KnishClassInstance(Statement.Class klass,
                       Environment closure,
                       Interpreter.InterpreterVisitor evaluator) {
        this.klass = klass;

        for (var method : klass.staticMethods) {
            Integer arity = null;
            if (method.argumentsNames != null) {
                arity = method.argumentsNames.size();
            }

            register(method.name, arity,
                    arguments -> {
                        Environment withParameters = new Environment(closure);
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
                    });
        }
    }

    @Override
    protected String getClassName() {
        return klass.name + " metaclass";
    }
}
