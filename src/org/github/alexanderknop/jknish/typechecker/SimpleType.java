package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.objects.KnishModule;
import org.github.alexanderknop.jknish.parser.MethodId;

import java.util.*;
import java.util.stream.Collectors;

class SimpleType {
    public static Top top() {
        return Top.TOP;
    }

    public static SimpleType bottom() {
        return Bottom.BOTTOM;
    }

    static class Variable extends SimpleType {
        public final Set<SimpleType> upperBound;
        public final Set<SimpleType> lowerBound;

        public Variable() {
            this.upperBound = new HashSet<>();
            this.lowerBound = new HashSet<>();
        }
    }

    static class Labeled extends SimpleType {
        public final String name;
        public final SimpleType type;

        public Labeled(String name, SimpleType type) {
            this.name = name;
            this.type = type;
        }
    }

    static class Class extends SimpleType {
        public final Map<MethodId, Method> methods;

        public Class(Map<MethodId, Method> methods) {
            this.methods = methods;
        }
    }

    static final class Top extends SimpleType {
        private static final Top TOP = new Top();
        private Top() {}
    }

    static final class Bottom extends SimpleType {
        private static final Bottom BOTTOM = new Bottom();
        private Bottom() {}
    }

    static class Method {
        public final List<SimpleType> arguments;
        public final SimpleType value;

        public Method(List<SimpleType> arguments, SimpleType value) {
            this.arguments = arguments;
            this.value = value;
        }
    }

    static Map<KnishModule.Class, SimpleType> fromKnishModule(KnishModule module) {
        Map<KnishModule.Class, SimpleType> types = new HashMap<>();
        Map<KnishModule.Class, SimpleType.Variable> classVariables = new HashMap<>();
        module.getClasses().forEach((className, klass) -> {
            SimpleType.Variable variable = new SimpleType.Variable();
            classVariables.put(klass, variable);
            types.put(klass, new SimpleType.Labeled(className, variable));
        });

        module.getClasses().values().forEach(
                klass -> fromKnishClass(klass, classVariables.get(klass), types));
        return types;
    }

    private static SimpleType fromKnishClass(KnishModule.Class klass,
                                      Map<KnishModule.Class, SimpleType> defined) {
        if (defined.containsKey(klass)) {
            return defined.get(klass);
        } else {
            SimpleType.Variable classVariable = new SimpleType.Variable();
            defined.put(klass, classVariable);

            return fromKnishClass(klass, classVariable, defined);
        }
    }

    private static SimpleType.Variable fromKnishClass(KnishModule.Class klass,
                                               SimpleType.Variable classVariable,
                                               Map<KnishModule.Class, SimpleType> defined) {
        Map<MethodId, SimpleType.Method> methods = new HashMap<>();
        SimpleType.Class simpleClass = new SimpleType.Class(methods);
        klass.getMethods().forEach(
                (methodId, method) -> methods.put(methodId, fromKnishMethod(method, defined)));

        classVariable.lowerBound.add(simpleClass);
        classVariable.upperBound.add(simpleClass);
        return classVariable;
    }

    private static SimpleType.Method fromKnishMethod(KnishModule.Method method,
                                              Map<KnishModule.Class, SimpleType> defined) {
        List<SimpleType> arguments = null;
        if (method.getArguments() != null) {
            arguments = method.getArguments().stream().map(
                    argument -> fromKnishIntersection(argument, defined)
            ).collect(Collectors.toList());
        }

        SimpleType value = fromKnishUnion(method.getValue(), defined);

        return new SimpleType.Method(arguments, value);
    }

    private static SimpleType fromKnishIntersection(KnishModule.Intersection intersection,
                                                    Map<KnishModule.Class, SimpleType> inProcess) {
        SimpleType.Variable variable = new SimpleType.Variable();
        intersection.getTypes().stream()
                .map(klass -> fromKnishClass(klass, inProcess))
                .forEach(variable.upperBound::add);
        return variable;
    }

    private static SimpleType fromKnishUnion(KnishModule.Union union,
                                      Map<KnishModule.Class, SimpleType> inProcess) {
        SimpleType.Variable variable = new SimpleType.Variable();
        union.getTypes().stream()
                .map(klass -> fromKnishClass(klass, inProcess))
                .forEach(variable.lowerBound::add);
        return variable;
    }
}
