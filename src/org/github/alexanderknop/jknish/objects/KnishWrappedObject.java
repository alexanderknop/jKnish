package org.github.alexanderknop.jknish.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnishWrappedObject<V> implements KnishObject {
    private final V value;
    private final Map<MethodId, Method<V>> methods;
    private final String name;

    private KnishWrappedObject(String name, V value,
                               Map<MethodId, Method<V>> methods) {
        this.value = value;
        this.methods = methods;
        this.name = name;
    }

    @Override
    public KnishObject call(String methodName, List<KnishObject> arguments) {
        Integer arity = (arguments == null) ? null : arguments.size();
        MethodId methodId = new MethodId(methodName, arity);
        Method<V> method = methods.get(methodId);
        if (method == null) {
            throw new KnishMethodNotFoundException(name, methodId);
        }
        return method.call(value, arguments);
    }

    interface Method<V> {
        KnishObject call(V value, List<KnishObject> arguments);
    }

    public static <V> OpenKnishWrappedObject<V> object(String name) {
        return new OpenKnishWrappedObject<>(name);
    }

    public static class OpenKnishWrappedObject<V> {
        private final String name;
        private final Map<MethodId, Method<V>> methods;
        private boolean closed = false;

        private OpenKnishWrappedObject(String name) {
            this.name = name;
            this.methods = new HashMap<>();
        }

        public OpenKnishWrappedObject<V> getter(String field, Method<V> method) {
            if (closed) {
                throw new UnsupportedOperationException("The class is already closed.");
            }

            methods.put(new MethodId(field, null), method);
            return this;
        }

        public OpenKnishWrappedObject<V> method(String field, int arity, Method<V> method) {
            if (closed) {
                throw new UnsupportedOperationException("The class is already closed.");
            }

            methods.put(new MethodId(field, arity), method);
            return this;
        }

        public KnishWrappedObject<V> construct(V value) {
            closed = true;
            return new KnishWrappedObject<>(name, value, methods);
        }
    }
}
