package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.Collections;

import static java.util.Collections.singleton;
import static org.github.alexanderknop.jknish.interpreter.InterpreterMethodUtils.compileMethod;
import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

class ClassInstance extends AbstractKnishObject {

    private final String name;

    ClassInstance(String name,
                  ResolvedStatement.Class klass,
                  Environment enclosing,
                  Interpreter.InterpreterVisitor evaluator) {
        this.name = name;

        Environment classEnvironment = new Environment(enclosing,
                singleton(klass.staticThisId));
        classEnvironment.set(klass.staticThisId, this);

        for (var method : klass.staticMethods) {
            register(method.name, arityFromArgumentsList(method.argumentsIds),
                    compileMethod(this, method, classEnvironment, evaluator));
        }

        // todo: add real constructors
        register("new", 0,
                arguments -> new Instance(name, klass, enclosing, evaluator));
    }

    @Override
    protected String getClassName() {
        return name + " metaclass";
    }
}
