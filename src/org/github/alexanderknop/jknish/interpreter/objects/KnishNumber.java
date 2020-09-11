package org.github.alexanderknop.jknish.interpreter.objects;

import org.github.alexanderknop.jknish.interpreter.KnishRuntimeException;

public class KnishNumber extends AbstractKnishObject {
    public static KnishNumber valueOf(long value) {
        return new KnishNumber(value);
    }

    private final Long value;

    private KnishNumber(Long value) {
        this.value = value;

        register("toString", null, (line, arguments) -> new KnishString(Long.toString(value)));
        register("+", 1, (line, arguments) -> {
            if (arguments.get(0) instanceof KnishNumber) {
                KnishNumber right = (KnishNumber) arguments.get(0);
                return valueOf(value + right.value);
            } else {
                throw new KnishRuntimeException(line, "Right operand must be a number.");
            }
        });
        register("-", 1, (line, arguments) -> {
            if (arguments.get(0) instanceof KnishNumber) {
                KnishNumber right = (KnishNumber) arguments.get(0);
                return valueOf(value - right.value);
            } else {
                throw new KnishRuntimeException(line, "Right operand must be a number.");
            }
        });
        register("-", null, (line, arguments) -> valueOf(-value));
        register("*", 1, (line, arguments) -> {
            if (arguments.get(0) instanceof KnishNumber) {
                KnishNumber right = (KnishNumber) arguments.get(0);
                return valueOf(value * right.value);
            } else {
                throw new KnishRuntimeException(line, "Right operand must be a number.");
            }
        });
        register("/", 1, (line, arguments) -> {
            if (arguments.get(0) instanceof KnishNumber) {
                KnishNumber right = (KnishNumber) arguments.get(0);
                return valueOf(value / right.value);
            } else {
                throw new KnishRuntimeException(line, "Right operand must be a number.");
            }
        });
    }

    @Override
    protected String getClassName() {
        return "Number";
    }
}