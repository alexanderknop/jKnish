package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import static org.github.alexanderknop.jknish.interpreter.InterpreterMethodUtils.compileMethod;

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
        klass.staticMethods.forEach(
                (methodId, method) ->
                        register(methodId, compileMethod(method, classEnvironment, evaluator))
        );

        // register all the constructors
        klass.constructors.forEach((methodId, constructor) -> register(
                methodId,
                arguments -> new Instance(
                        name, klass, classEnvironment, evaluator,
                        constructor, arguments
                )
        ));
    }

    @Override
    protected String getClassName() {
        return name + " metaclass";
    }
}
