package org.github.alexanderknop.jknish.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public abstract class KnishModule {
    private final Map<String, KnishClassBuilder> classBuilders = new HashMap<>();
    private Map<String, KnishClass> classes;
    private final Map<String, KnishObject> objects = new HashMap<>();
    private final Map<String, String> objectsClasses = new HashMap<>();

    public Map<String, KnishObject> getObjects() {
        return objects;
    }

    public KnishClass getClass(String className) {
        return classes.get(className);
    }
    public KnishClass getObjectType(String objectName) {
        return getClass(objectsClasses.get(objectName));
    }

    protected KnishClassBuilder declareClass(String className) {
        KnishClassBuilder builder = new KnishClassBuilder(className);
        classBuilders.put(className, builder);
        return builder;
    }

    protected void define(String name,
                          KnishObject object, KnishClassBuilder classBuilder) {
        objects.put(name, object);
        objectsClasses.put(name, classBuilder.name);
    }

    protected void finishBuilding() {
        if (classes != null) {
            throw new UnsupportedOperationException("Module construction is already finished");
        }

        classes = new HashMap<>();
        classBuilders.keySet().forEach(name -> classes.put(name, new KnishClass()));
        for (String name : classBuilders.keySet()) {
            Map<MethodId, MethodBuilder> builderMethods = classBuilders.get(name).methods;
            Map<MethodId, Method> methods = new HashMap<>();
            builderMethods.forEach(
                    (methodName, methodBuilder) -> methods.put(
                            methodName,
                            new Method(
                                    methodBuilder.arguments == null ? null :
                                            unmodifiableList(methodBuilder.arguments.stream().map(
                                                    argument -> classes.get(argument.name)
                                            ).collect(Collectors.toList())),
                                    classes.get(methodBuilder.value.name)
                            )
                    )
            );
            classes.get(name).methods = methods;
        }
    }

    protected KnishClassBuilder top() {
        // TODO
        throw new UnsupportedOperationException("Top is not supported, yet");
    }

    protected static class KnishClassBuilder {
        private final String name;
        private final Map<MethodId, MethodBuilder> methods;

        private KnishClassBuilder(String name) {
            this.name = name;
            methods = new HashMap<>();
        }

        public KnishClassBuilder method(String methodName,
                                        List<KnishClassBuilder> arguments,
                                        KnishClassBuilder value) {
            methods.put(new MethodId(methodName, arguments.size()),
                    new MethodBuilder(arguments, value));
            return this;
        }

        public KnishClassBuilder getter(String field,
                                        KnishClassBuilder value) {
            methods.put(new MethodId(field, null),
                    new MethodBuilder(emptyList(), value));
            return this;
        }
    }

    private static class MethodBuilder {
        private final List<KnishClassBuilder> arguments;
        private final KnishClassBuilder value;

        private MethodBuilder(List<KnishClassBuilder> arguments, KnishClassBuilder value) {
            this.arguments = arguments;
            this.value = value;
        }
    }

    public static class KnishClass {
        private Map<MethodId, Method> methods;

        private KnishClass() {
        }

        public Map<MethodId, Method> getMethods() {
            return methods;
        }
    }

    public static class Method {
        public final List<KnishClass> arguments;
        public final KnishClass value;

        private Method(List<KnishClass> arguments, KnishClass value) {
            this.arguments = arguments;
            this.value = value;
        }
    }
}
