package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.objects.KnishObject;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.List;

import static org.github.alexanderknop.jknish.interpreter.InterpreterMethodUtils.compileMethod;

class Instance extends AbstractKnishObject {
    private final String name;

    public Instance(String name,
                    ResolvedStatement.Class klass,
                    Environment enclosing,
                    Interpreter.InterpreterVisitor evaluator,
                    ResolvedStatement.Method constructor,
                    List<KnishObject> arguments,
                    KnishObject nilValue) {
        this.name = name;

        // define an environment with all the fields
        Environment classEnvironment =
                new Environment(enclosing, klass.fields.keySet());
        classEnvironment.set(klass.thisId, this);

        // register all the methods
        klass.methods.forEach((methodId, method) ->
                register(methodId,
                        compileMethod(method, classEnvironment, evaluator, nilValue)));

        // call the constructor
        compileMethod(constructor, classEnvironment, evaluator, nilValue).call(arguments);
    }

    @Override
    protected String getClassName() {
        return name;
    }
}
