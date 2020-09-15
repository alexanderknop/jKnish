package org.github.alexanderknop.jknish.objects;

import java.util.Objects;

public final class MethodId {
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
