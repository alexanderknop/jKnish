package org.github.alexanderknop.jknish.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractKnishObject implements KnishObject {

    abstract protected String getClassName();

    private final Map<MethodId, KnishMethod> methods = new HashMap<>();

    public void register(String name, Integer arity, KnishMethod method) {
        methods.put(new MethodId(name, arity), method);
    }

    @Override
    public KnishObject call(String methodName, List<KnishObject> arguments) {
        Integer arity = (arguments == null) ? null : arguments.size();
        MethodId methodId = new MethodId(methodName, arity);
        KnishMethod method = methods.get(methodId);
        if (method == null) {
            throw new KnishMethodNotFoundException(getClassName(), methodId);
        }
        return method.call(arguments);
    }

    public interface KnishMethod {
        KnishObject call(List<KnishObject> arguments);
    }
}
