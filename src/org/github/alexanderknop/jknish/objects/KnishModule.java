package org.github.alexanderknop.jknish.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;

public abstract class KnishModule {
    private final Map<String, Class> classBuilders = new HashMap<>();
    private final Map<String, KnishObject> objects = new HashMap<>();
    private final Map<String, Class> objectsClasses = new HashMap<>();

    public Map<String, KnishObject> getObjects() {
        return objects;
    }

    public Class getClass(String className) {
        return classBuilders.get(className);
    }

    public Class getObjectType(String objectName) {
        return objectsClasses.get(objectName);
    }

    protected Class declareClass(String className) {
        Class builder = new Class();
        classBuilders.put(className, builder);
        return builder;
    }

    protected Class anonymousClass() {
        return new Class();
    }

    protected KnishType top() {
        return new Top();
    }

    protected KnishType bottom() {
        return new Bottom();
    }

    protected KnishType union(Set<KnishType> types) {
        return new Union(types);
    }

    protected KnishType intersection(Set<KnishType> types) {
        return new Intersection(types);
    }

    protected void define(String name,
                          KnishObject object, Class klass) {
        objects.put(name, object);
        objectsClasses.put(name, klass);
    }

    public static class KnishType {

        private KnishType() {
        }
    }

    public final static class Top extends KnishType {
    }

    public static final class Bottom extends KnishType {
    }

    public final static class Union extends KnishType {
        private final Set<KnishType> types;

        private Union(Set<KnishType> types) {
            this.types = types;
        }
    }

    public final static class Intersection extends KnishType {
        private final Set<KnishType> types;

        private Intersection(Set<KnishType> types) {
            this.types = types;
        }
    }

    public static final class Class extends KnishType {
        private final Map<MethodId, Method> methods;

        private Class() {
            methods = new HashMap<>();
        }

        protected Class method(String methodName,
                               List<Class> arguments,
                               Class value) {
            methods.put(new MethodId(methodName, arguments.size()),
                    new Method(arguments, value));
            return this;
        }

        protected Class getter(String field,
                               Class value) {
            methods.put(new MethodId(field, null),
                    new Method(emptyList(), value));
            return this;
        }
    }

    private static class Method {
        private final List<Class> arguments;
        private final Class value;

        private Method(List<Class> arguments, Class value) {
            this.arguments = arguments;
            this.value = value;
        }
    }
}
