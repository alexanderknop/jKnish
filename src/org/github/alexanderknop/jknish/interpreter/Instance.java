package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.AbstractKnishObject;
import org.github.alexanderknop.jknish.objects.KnishObject;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.List;

import static org.github.alexanderknop.jknish.interpreter.InterpreterMethodUtils.compileMethod;
import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

class Instance extends AbstractKnishObject {
    private final String name;

    public Instance(String name,
                    ResolvedStatement.Class klass,
                    Environment enclosing,
                    Interpreter.InterpreterVisitor evaluator,
                    ResolvedStatement.Method constructor,
                    List<KnishObject> arguments) {
        this.name = name;

        // define an environment with all the fields
        Environment classEnvironment =
                new Environment(enclosing, klass.fields.keySet());
        classEnvironment.set(klass.thisId, this);

        // register all the methods
        for (var method : klass.methods) {
            register(method.name, arityFromArgumentsList(method.argumentsIds),
                    compileMethod(method, classEnvironment, evaluator));
        }

        // call the constructor
        compileMethod(constructor, classEnvironment, evaluator).call(arguments);
    }

    @Override
    protected String getClassName() {
        return name;
    }
}
