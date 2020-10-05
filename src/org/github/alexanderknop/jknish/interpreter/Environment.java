package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Environment enclosing;
    private final Map<Integer, KnishObject> objects = new HashMap<>();

    public Environment(Collection<Integer> variables) {
        this(null, variables);
    }

    public Environment(Environment enclosing,
                       Collection<Integer> variables) {
        this.enclosing = enclosing;
        variables.forEach(variable -> define(variable, KnishCore.core().nil()));
    }

    private void define(int id, KnishObject value) {
        objects.put(id, value);
    }

    public KnishObject get(int id) {
        if (objects.containsKey(id)) {
            return objects.get(id);
        }

        if (enclosing != null) {
            return enclosing.get(id);
        }

        throw new UnsupportedOperationException(
                "Undefined variable with id equal to " + id + ".");
    }

    public KnishObject set(int id, KnishObject value) {
        if (objects.containsKey(id)) {
            objects.put(id, value);
            return value;
        }

        if (enclosing != null) {
            return enclosing.set(id, value);
        }

        throw new UnsupportedOperationException(
                "Undefined variable with id equal to " + id + ".");
    }
}
