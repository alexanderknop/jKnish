package org.github.alexanderknop.jknish.objects;

import org.github.alexanderknop.jknish.parser.MethodId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class KnishWrappedObject<V> extends AbstractKnishObject {
    private final V value;
    private final String name;

    public static <U> U unwrap(KnishObject object,
                               Class<U> uClass,
                               String message) {
        if (object instanceof KnishWrappedObject &&
                ((KnishWrappedObject<?>) object).getValue().getClass() == uClass) {
            return ((KnishWrappedObject<U>) object).getValue();
        }
        throw new KnishRuntimeException(message);
    }

    private KnishWrappedObject(String name, V value) {
        this.value = value;
        this.name = name;
    }

    public V getValue() {
        return value;
    }

    @Override
    protected String getClassName() {
        return this.name;
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
            KnishWrappedObject<V> object = new KnishWrappedObject<>(name, value);
            methods.forEach((id, method) ->
                    object.register(id, arguments -> method.call(value, arguments)));
            return object;
        }
    }
}
