package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import static org.github.alexanderknop.jknish.interpreter.InterpreterMethodUtils.compileMethod;
import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

class ClassInstance extends AbstractKnishObject {

    private final String name;

    ClassInstance(String name,
                  ResolvedStatement.Class klass,
                  Environment enclosing,
                  Interpreter.InterpreterVisitor evaluator) {
        this.name = name;

        // define an environment with all the static fields
        Environment classEnvironment =
                new Environment(enclosing, klass.staticFields.keySet());
        classEnvironment.set(klass.staticThisId, this);

        // register all the static methods
        for (var method : klass.staticMethods) {
            register(method.name, arityFromArgumentsList(method.argumentsIds),
                    compileMethod(this, method, classEnvironment, evaluator));
        }

        // register all the constructors
        for (ResolvedStatement.Method constructor : klass.constructors) {
            Integer arity = MethodId.arityFromArgumentsList(constructor.argumentsIds);
            register(
                    constructor.name, arity,
                    arguments -> new Instance(
                            name, klass, enclosing, evaluator,
                            constructor, arguments
                    )
            );
        }
    }

    @Override
    protected String getClassName() {
        return name + " metaclass";
    }
}
