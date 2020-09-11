package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.KnishObject;

import java.util.HashMap;
import java.util.Map;

class Environment {
    private final Environment enclosing;
    private final Map<String, KnishObject> objects;

    public Environment() {
        this(null);
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
        this.objects = new HashMap<>();
    }

    public KnishObject get(int line, String name) {
        if (objects.containsKey(name)) {
            return objects.get(name);
        }

        if (enclosing != null) {
            return enclosing.get(line, name);
        }

        throw new KnishRuntimeExceptionWithLine(line, "Undefined variable " + name + ".");
    }

    public void define(String name, KnishObject value) {
        objects.put(name, value);
    }

    public KnishObject set(int line, String name, KnishObject value) {
        if (objects.containsKey(name)) {
            objects.put(name, value);
            return value;
        }

        if (enclosing != null) {
            return enclosing.set(line, name, value);
        }

        throw new KnishRuntimeExceptionWithLine(line, "Undefined variable " + name + ".");
    }
}
