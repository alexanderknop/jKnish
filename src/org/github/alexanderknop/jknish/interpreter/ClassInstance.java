package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.parser.Statement;

import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

class ClassInstance extends AbstractKnishObject {
    private final Statement.Class klass;

    ClassInstance(Statement.Class klass,
                  Environment enclosing,
                  Interpreter.InterpreterVisitor evaluator) {
        this.klass = klass;

        for (var method : klass.staticMethods) {
            register(method.name, arityFromArgumentsList(method.argumentsNames),
                    InterpreterMethodUtils.compileMethod(this, method, enclosing, evaluator));
        }

        // todo: add real constructors
        register("new", 0,
                arguments -> new Instance(klass, enclosing, evaluator));
    }

    @Override
    protected String getClassName() {
        return klass.name + " metaclass";
    }
}
