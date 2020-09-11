package org.github.alexanderknop.jknish.interpreter.objects;

import org.github.alexanderknop.jknish.KnishObject;
import org.github.alexanderknop.jknish.interpreter.KnishRuntimeException;

import java.util.List;

public class KnishString extends AbstractKnishObject {
    private final String value;

    public KnishString(String value) {
        this.value = value;

        register("toString", null, (line, arguments) -> this);
    }

    @Override
    protected String getClassName() {
        return "String";
    }

    @Override
    public String toString() {
        return value;
    }
}
