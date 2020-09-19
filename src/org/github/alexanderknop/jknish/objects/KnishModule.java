package org.github.alexanderknop.jknish.objects;

import org.github.alexanderknop.jknish.parser.MethodId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.*;

public abstract class KnishModule {
    private final Map<String, Class> classes = new HashMap<>();
    private final Map<String, KnishObject> objects = new HashMap<>();
    private final Map<String, Class> objectsClasses = new HashMap<>();

    public Map<String, KnishObject> getObjects() {
        return unmodifiableMap(objects);
    }

    public Class getClass(String className) {
        return classes.get(className);
    }

    public Class getObjectType(String objectName) {
        return objectsClasses.get(objectName);
    }

    protected Class declareClass(String className) {
        Class builder = new Class();
        classes.put(className, builder);
        return builder;
    }

    protected Class anonymousClass() {
        return new Class();
    }

    protected Intersection top() {
        return new Intersection(emptySet());
    }

    protected Union bottom() {
        return new Union(emptySet());
    }

    protected Union union(Set<Class> types) {
        return new Union(types);
    }

    protected Intersection intersection(Set<Class> types) {
        return new Intersection(types);
    }

    protected void define(String name,
                          KnishObject object, Class klass) {
        objects.put(name, object);
        objectsClasses.put(name, klass);
    }

    public Map<String, Class> getClasses() {
        return unmodifiableMap(classes);
    }

    public final static class Union {
        private final Set<Class> types;

        private Union(Set<Class> types) {
            this.types = types;
        }

        public Set<Class> getTypes() {
            return unmodifiableSet(types);
        }
    }

    public final static class Intersection {
        private final Set<Class> types;

        private Intersection(Set<Class> types) {
            this.types = types;
        }

        public Set<Class> getTypes() {
            return unmodifiableSet(types);
        }
    }

    public static final class Class {
        private final Map<MethodId, Method> methods;

        private Class() {
            methods = new HashMap<>();
        }

        protected Class method(String methodName,
                               List<Intersection> arguments,
                               Union value) {
            methods.put(new MethodId(methodName, arguments.size()),
                    new Method(arguments, value));
            return this;
        }

        protected Class method(String methodName,
                               List<Class> arguments,
                               Class value) {
            methods.put(new MethodId(methodName, arguments.size()),
                    new Method(arguments, value));
            return this;
        }

        protected Class getter(String field,
                               Union value) {
            methods.put(new MethodId(field, null),
                    new Method(null, value));
            return this;
        }

        protected Class getter(String field,
                               Class value) {
            methods.put(new MethodId(field, null),
                    new Method(null, value));
            return this;
        }

        public Map<MethodId, Method> getMethods() {
            return unmodifiableMap(methods);
        }
    }

    public static class Method {
        private final List<Intersection> arguments;
        private final Union value;

        private Method(List<Intersection> arguments, Union value) {
            this.arguments = arguments;
            this.value = value;
        }

        public Method(List<Class> arguments, Class value) {
            this(
                    arguments == null ? null :
                            arguments.stream()
                                    .map(t -> new Intersection(singleton(t)))
                                    .collect(Collectors.toList()),
                    new Union(singleton(value))
            );
        }

        public List<Intersection> getArguments() {
            return arguments != null ? unmodifiableList(arguments) : null;
        }

        public Union getValue() {
            return value;
        }
    }
}
