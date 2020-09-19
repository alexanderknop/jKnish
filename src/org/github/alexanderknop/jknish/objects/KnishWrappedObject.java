package org.github.alexanderknop.jknish.objects;

import org.github.alexanderknop.jknish.parser.MethodId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KnishWrappedObject<V> implements KnishObject {
    private final V value;
    private final Map<MethodId, ? extends Method<V>> methods;
    private final String name;

    private KnishWrappedObject(String name, V value,
                               Map<MethodId, ? extends Method<V>> methods) {
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
            throw new MethodNotFoundException(name, methodId);
        }
        return method.call(value, arguments);
    }

    interface Method<V> {
        KnishObject call(V value, List<KnishObject> arguments);
    }

    public static <V> KnishWrappedObjectConstructor<V> object(String name) {
        return new KnishWrappedObjectConstructor<>(name);
    }

    public static final class KnishWrappedObjectConstructor<V> {
        private final String name;
        private final Map<MethodId, Method<V>> methods;
        private boolean closed = false;

        private KnishWrappedObjectConstructor(String name) {
            this.name = name;
            this.methods = new HashMap<>();
        }

        public KnishWrappedObjectConstructor<V> getter(String field, Method<V> method) {
            if (closed) {
                throw new UnsupportedOperationException("The class is already closed.");
            }

            methods.put(new MethodId(field, null), method);
            return this;
        }

        public KnishWrappedObjectConstructor<V> method(String field, int arity, Method<V> method) {
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
