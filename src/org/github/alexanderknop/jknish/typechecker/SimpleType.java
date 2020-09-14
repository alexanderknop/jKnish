package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.objects.MethodId;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleType {
    public static class Variable extends SimpleType {
        public final Set<SimpleType> upperBound;
        public final Set<SimpleType> lowerBound;

        public Variable() {
            this.upperBound = new HashSet<>();
            this.lowerBound = new HashSet<>();
        }
    }

    public static class Labeled extends SimpleType {
        public final String name;
        public final SimpleType type;

        public Labeled(String name, SimpleType type) {
            this.name = name;
            this.type = type;
        }
    }

    public static class Class extends SimpleType {
        public final Map<MethodId, Method> methods;

        public Class(Map<MethodId, Method> methods) {
            this.methods = methods;
        }
    }

    public static class Method {
        public final List<SimpleType> arguments;
        public final SimpleType value;

        public Method(List<SimpleType> arguments, SimpleType value) {
            this.arguments = arguments;
            this.value = value;
        }
    }
}
