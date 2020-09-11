package org.github.alexanderknop.jknish;

import java.util.*;
import java.util.stream.Collectors;

public class Class {
    public final String name;
    public final Map<String, Map<Integer, Method>> methods;

    private Class(String name) {
        this.name = name;
        this.methods = new HashMap<>();
    }

    public static class Method {
        final List<Class> arguments;
        final Class value;

        private Method(List<Class> arguments, Class value) {
            this.arguments = arguments;
            this.value = value;
        }

        @Override
        public String toString() {
            if (arguments != null) {
                return "(" +
                        arguments.stream().map(c -> c.name).collect(Collectors.joining(", ")) +
                        ") -> " + value.name;
            } else {
                return value.name;
            }
        }
    }

    public static RawClass defineClass(String name) {
        return new RawClass(name);
    }

    @Override
    public String toString() {
        List<String> methodsRepresentations = new ArrayList<>();

        for (Map.Entry<String, Map<Integer, Method>> methodVariants :
                methods.entrySet()) {
            for (Map.Entry<Integer, Method> method :
                    methodVariants.getValue().entrySet()) {
                methodsRepresentations.add(
                        methodVariants.getKey() + " : " + method.getValue()
                );
            }
        }

        return name + "{" + String.join(", ", methodsRepresentations) + "}";
    }

    public static class RawClass {
        private final String name;
        private final Map<String, Map<Integer, RawMethod>> methods;

        private RawClass(String name) {
            this.name = name;
            methods = new HashMap<>();
        }

        public RawClass defineMethod(String methodName,
                                     List<RawClass> arguments, RawClass value) {
            if (!methods.containsKey(methodName)) {
                methods.put(methodName, new HashMap<>());
            }

            Map<Integer, RawMethod> methodVariants = methods.get(methodName);
            methodVariants.put(arguments.size(), new RawMethod(arguments, value));

            return this;
        }

        public RawClass defineGetter(String fieldName, RawClass value) {
            if (!methods.containsKey(fieldName)) {
                methods.put(fieldName, new HashMap<>());
            }
            Map<Integer, RawMethod> methodVariants = methods.get(fieldName);
            methodVariants.put(null, new RawMethod(null, value));

            return this;
        }

        public RawClass defineSetter(String fieldName, RawClass value) {
            String methodName = fieldName + "=";
            if (!methods.containsKey(methodName)) {
                methods.put(methodName, new HashMap<>());
            }
            Map<Integer, RawMethod> methodVariants = methods.get(methodName);
            methodVariants.put(1, new RawMethod(List.of(value), value));

            return this;
        }

        public Class finish() {
            return finish(new HashMap<>());
        }

        private Class finish(Map<RawClass, Class> cash) {
            if (cash.containsKey(this)) {
                return cash.get(this);
            }

            Class result = new Class(name);
            cash.put(this, result);

            for (Map.Entry<String, Map<Integer, RawMethod>> methodVariants :
                    methods.entrySet()) {
                HashMap<Integer, Class.Method> variants = new HashMap<>();
                for (Map.Entry<Integer, RawMethod> method :
                        methodVariants.getValue().entrySet()) {

                    Class value = method.getValue().value.finish(cash);
                    List<Class> arguments = null;
                    if (method.getValue().arguments != null) {
                        arguments = method.getValue().arguments.stream()
                                .map(RawClass::finish)
                                .collect(Collectors.toList());
                        arguments = Collections.unmodifiableList(arguments);
                    }
                    variants.put(method.getKey(),
                            new Class.Method(arguments, value));

                }
                result.methods.put(methodVariants.getKey(),
                        Collections.unmodifiableMap(variants));
            }

            return result;
        }

        private class RawMethod {
            private final List<RawClass> arguments;
            private final RawClass value;

            public RawMethod(List<RawClass> arguments, RawClass value) {
                this.arguments = arguments;
                this.value = value;
            }
        }
    }
}
