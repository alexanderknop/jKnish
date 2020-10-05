package org.github.alexanderknop.jknish.objects;

import org.github.alexanderknop.jknish.parser.MethodId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

public abstract class AbstractKnishObject implements KnishObject {

    abstract protected String getClassName();

    private final Map<MethodId, Method> methods = new HashMap<>();

    public AbstractKnishObject() {
        // register default object methods
        register("!==", 1,
                arguments -> KnishCore.core().bool(arguments.get(0) != this));
        register("===", 1,
                arguments -> KnishCore.core().bool(arguments.get(0) == this));
    }

    public void register(String name, Integer arity, Method method) {
        methods.put(new MethodId(name, arity), method);
    }

    public void register(MethodId methodId, Method method) {
        methods.put(methodId, method);
    }

    @Override
    public KnishObject call(String methodName, List<KnishObject> arguments) {
        Integer arity = arityFromArgumentsList(arguments);
        MethodId methodId = new MethodId(methodName, arity);
        Method method = methods.get(methodId);
        if (method == null) {
            throw new MethodNotFoundException(getClassName(), methodId);
        }
        return method.call(arguments);
    }

    public interface Method {
        KnishObject call(List<KnishObject> arguments);
    }
}
