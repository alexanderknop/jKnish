package org.github.alexanderknop.jknish.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;

public abstract class AbstractKnishObject implements KnishObject {

    abstract protected String getClassName();

    private final Map<String, Map<Integer, KnishMethod>> methods = new HashMap<>();

    public void register(String name, Integer arity, KnishMethod method) {
        if (!methods.containsKey(name)) {
            methods.put(name, new HashMap<>());
        }

        methods.get(name).put(arity, method);
    }

    @Override
    public KnishObject call(String methodName, List<KnishObject> arguments) {
        Integer arity = (arguments == null) ? null : arguments.size();
        KnishMethod method = methods.getOrDefault(methodName, emptyMap()).get(arity);
        if (method == null) {
            throw new KnishMethodNotFoundException(getClassName(), methodName, arity);
        }
        return method.call(arguments);
    }

    public interface KnishMethod {
        KnishObject call(List<KnishObject> arguments);
    }
}
