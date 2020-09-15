package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.parser.Statement;

class KnishClassInstance extends AbstractKnishObject {
    private final Statement.Class klass;
    private final Interpreter.InterpreterVisitor evaluator;

    KnishClassInstance(Statement.Class klass,
                       Environment enclosing,
                       Interpreter.InterpreterVisitor evaluator) {
        this.klass = klass;
        this.evaluator = evaluator;

        for (var method : klass.staticMethods) {
            Integer arity = null;
            if (method.argumentsNames != null) {
                arity = method.argumentsNames.size();
            }

            register(method.name, arity,
                    MethodUtils.compileMethod(method, enclosing, evaluator));
        }

        register("new", 0,
                arguments -> new KnishInstance(klass, enclosing, evaluator));
    }

    @Override
    protected String getClassName() {
        return klass.name + " metaclass";
    }
}