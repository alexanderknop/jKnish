package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.parser.Statement;

import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

class Instance extends AbstractKnishObject {
    private final Statement.Class klass;

    public Instance(Statement.Class klass,
                    Environment enclosing,
                    Interpreter.InterpreterVisitor evaluator) {
        this.klass = klass;
        for (var method : klass.methods) {
            register(method.name, arityFromArgumentsList(method.argumentsNames),
                    InterpreterMethodUtils.compileMethod(this, method, enclosing, evaluator));
        }
    }

    @Override
    protected String getClassName() {
        return klass.name;
    }
}
