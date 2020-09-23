package org.github.alexanderknop.jknish.parser;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MethodId {
    public static <V> Integer arityFromArgumentsList(List<V> arguments) {
        if (arguments == null) {
            return null;
        } else {
            return arguments.size();
        }
    }

    public static <U, V> List<U> processArgumentsList(
            List<V> arguments, Function<V, U> mapper) {
        if (arguments == null) {
            return null;
        } else {
            return arguments.stream().map(mapper).collect(Collectors.toList());
        }
    }

    public final String name;
    public final Integer arity;

    public MethodId(String name, Integer arity) {
        this.name = name;
        this.arity = arity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodId methodId = (MethodId) o;
        return Objects.equals(name, methodId.name) &&
                Objects.equals(arity, methodId.arity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, arity);
    }

    @Override
    public String toString() {
        String arguments;
        if (arity == null) {
            arguments = "";
        } else if (arity == 0) {
            arguments = "()";
        } else {
            arguments = "_, ".repeat(arity);
            arguments = "(" + arguments.substring(0, arguments.length() - 2) + ")";
        }
        return name + arguments;
    }
}
