package org.github.alexanderknop.jknish.objects;

import org.github.alexanderknop.jknish.objects.KnishWrappedObject.KnishWrappedObjectConstructor;
import org.github.alexanderknop.jknish.parser.MethodId;

import java.util.*;

import static java.util.Collections.*;
import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;
import static org.github.alexanderknop.jknish.parser.MethodId.processArgumentsList;

public abstract class KnishModule {
    private final Map<String, Class> classes = new HashMap<>();
    private final Map<String, KnishObject> objects = new HashMap<>();
    private final Map<String, Class> objectsClasses = new HashMap<>();

    public Map<String, KnishObject> getObjects() {
        return unmodifiableMap(objects);
    }

    public Map<String, Class> getClasses() {
        return unmodifiableMap(classes);
    }

    protected KnishModule() {
        importModules(KnishCore.core());
    }

    public void importModules(KnishModule... modules) {
        Arrays.stream(modules).forEach(module -> {
            if (module != null) {
                objects.putAll(module.getObjects());
                classes.putAll(module.getClasses());
            }
        });
    }

    public Map<String, Class> getObjectTypes() {
        return new HashMap<>(objectsClasses);
    }

    protected Class declareClass(String className) {
        Class builder = new Class(className);
        classes.put(className, builder);
        return builder;
    }

    protected Class anonymousClass() {
        return new Class(null);
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

    protected Union union(Class... types) {
        return new Union(new HashSet<>(Arrays.asList(types)));
    }

    protected Intersection intersection(Set<Class> types) {
        return new Intersection(types);
    }

    protected Intersection intersection(Class... types) {
        return new Intersection(new HashSet<>(Arrays.asList(types)));
    }

    protected void define(String name,
                          KnishObject object, Class klass) {
        objects.put(name, object);
        objectsClasses.put(name, klass);
    }

    protected <U, V> ClassDefinition<U, V> defineClass(String name) {
        return new ClassDefinition<>(name);
    }

    public Class numType() {
        return getClasses().get("Num");
    }

    public Class boolType() {
        return getClasses().get("Bool");
    }

    public Class stringType() {
        return getClasses().get("String");
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
        private final String name;

        private Class(String name) {
            this.name = name;
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

        public String getName() {
            return name;
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
                    processArgumentsList(
                            arguments,
                            t -> new Intersection(singleton(t))
                    ),
                    new Union(singleton(value))
            );
        }

        public List<Intersection> getArguments() {
            return arguments == null ? null : unmodifiableList(arguments);
        }

        public Union getValue() {
            return value;
        }
    }

    protected class ClassDefinition<U, V> {
        private final String name;
        private final Class metaClass;
        private final Class klass;
        private final KnishWrappedObjectConstructor<U> staticInstance;
        private final KnishWrappedObjectConstructor<V> instance;

        private ClassDefinition(String name) {
            this.name = name;
            this.metaClass = declareClass(name + " metaclass");
            this.klass = declareClass(name);
            staticInstance = KnishWrappedObject.object(name + " metaclass");
            instance = KnishWrappedObject.object(name);

            klass.method("===", List.of(top()), union(boolType()));
            klass.method("!==", List.of(top()), union(boolType()));
            metaClass.method("===", List.of(top()), union(boolType()));
            metaClass.method("!==", List.of(top()), union(boolType()));

        }

        protected ClassDefinition<U, V> staticMethod(String methodName,
                                                     List<Class> arguments,
                                                     Class value,
                                                     KnishWrappedObject.Method<U> method) {
            staticInstance.method(methodName, arityFromArgumentsList(arguments), method);
            metaClass.method(methodName, arguments, value);
            return this;
        }

        protected ClassDefinition<U, V> method(String methodName,
                                                     List<Class> arguments,
                                                     Class value,
                                                     KnishWrappedObject.Method<V> method) {
            instance.method(methodName, arityFromArgumentsList(arguments), method);
            klass.method(methodName, arguments, value);
            return this;
        }

        protected ClassDefinition<U,V> staticGetter(String methodName,
                                                    Class value,
                                                    KnishWrappedObject.Method<U> method) {
            staticInstance.getter(methodName, method);
            metaClass.getter(methodName, value);
            return this;
        }

        protected void finishDefinition(U state) {
            define(name, staticInstance.construct(state), metaClass);
        }

        protected KnishObject construct(V value) {
            return instance.construct(value);
        }

        public Class getMetaClass() {
            return metaClass;
        }

        public Class getInstanceClass() {
            return klass;
        }

        public ClassDefinition<U, V> getter(String methodName,
                                            Class value,
                                            KnishWrappedObject.Method<V> method) {
            instance.getter(methodName, method);
            klass.getter(methodName, value);
            return this;
        }
    }
}
