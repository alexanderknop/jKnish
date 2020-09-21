package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import static java.util.Collections.singleton;
import static org.github.alexanderknop.jknish.interpreter.InterpreterMethodUtils.compileMethod;
import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

class Instance extends AbstractKnishObject {
    private final String name;

    public Instance(String name,
                    ResolvedStatement.Class klass,
                    Environment enclosing,
                    Interpreter.InterpreterVisitor evaluator) {
        this.name = name;

        Environment classEnvironment = new Environment(enclosing,
                singleton(klass.thisId));
        classEnvironment.set(klass.thisId, this);

        for (var method : klass.methods) {
            register(method.name, arityFromArgumentsList(method.argumentsIds),
                    compileMethod(this, method, classEnvironment, evaluator));
        }
    }

    @Override
    protected String getClassName() {
        return name;
    }
}
