package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.parser.Statement;

public class KnishInstance extends AbstractKnishObject {
    private final Statement.Class klass;

    public KnishInstance(Statement.Class klass,
                         Environment enclosing,
                         Interpreter.InterpreterVisitor evaluator) {
        this.klass = klass;
        for (var method : klass.methods) {
            Integer arity = null;
            if (method.argumentsNames != null) {
                arity = method.argumentsNames.size();
            }

            register(method.name, arity,
                    InterpreterMethodUtils.compileMethod(method, enclosing, evaluator));
        }
    }

    @Override
    protected String getClassName() {
        return klass.name;
    }
}
