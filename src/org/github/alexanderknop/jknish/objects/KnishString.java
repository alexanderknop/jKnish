package org.github.alexanderknop.jknish.objects;

public class KnishString extends AbstractKnishObject {
    private final String value;

    public KnishString(String value) {
        this.value = value;

        register("toString", null, (line, arguments) -> this);
        register("+", 1, (line, arguments) -> {
            if (arguments.get(0) instanceof KnishString) {
                KnishString right = (KnishString) arguments.get(0);
                return new KnishString(value + right.value);
            } else {
                throw new KnishRuntimeException("Right operand must be a string.");
            }
        });
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
