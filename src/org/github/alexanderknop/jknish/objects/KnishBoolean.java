package org.github.alexanderknop.jknish.objects;

import java.util.Objects;

public class KnishBoolean extends AbstractKnishObject {
    public static final KnishBoolean TRUE = new KnishBoolean(true);
    public static final KnishBoolean FALSE = new KnishBoolean(false);

    public static KnishBoolean valueOf(Boolean value) {
        if (value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    private final Boolean value;

    private KnishBoolean(Boolean value) {
        this.value = value;

        register("toString", null, (line, arguments) -> new KnishString(Boolean.toString(value)));
    }

    @Override
    protected String getClassName() {
        return "Boolean";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnishBoolean that = (KnishBoolean) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
