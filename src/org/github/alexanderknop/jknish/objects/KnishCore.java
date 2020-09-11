package org.github.alexanderknop.jknish.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class KnishCore implements KnishModule {
    private final Map<String, KnishObject> objects;

    public KnishCore(Writer output) {
        objects = Map.of(
                "System", new KnishSystem(output)
        );
    }

    @Override
    public Map<String, KnishObject> getObjects() {
        return objects;
    }

    public static class KnishNumber extends AbstractKnishObject {
        public static KnishNumber valueOf(long value) {
            return new KnishNumber(value);
        }

        private final Long value;

        private KnishNumber(Long value) {
            this.value = value;

            register("toString", null, (arguments) -> new KnishString(Long.toString(value)));
            register("+", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return valueOf(value + right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("-", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return valueOf(value - right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("-", null, arguments -> valueOf(-value));
            register("*", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return valueOf(value * right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("/", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return valueOf(value / right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });

            register("<", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return KnishBoolean.valueOf(value < right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register(">", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return KnishBoolean.valueOf(value > right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("<=", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return KnishBoolean.valueOf(value <= right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register(">=", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return KnishBoolean.valueOf(value >= right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("==", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return KnishBoolean.valueOf(value.equals(right.value));
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("!=", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return KnishBoolean.valueOf(!value.equals(right.value));
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
        }

        @Override
        protected String getClassName() {
            return "Number";
        }
    }

    public static class KnishSystem extends AbstractKnishObject {
        public KnishSystem(Writer output) {
            register("print", 1, arguments -> {
                try {
                    KnishObject string =
                            arguments.get(0).call("toString", null);
                    if (!(string instanceof KnishString)) {
                        throw new KnishRuntimeException("toString must return a String.");
                    }
                    output.write(string.toString());
                    output.write("\n");
                    output.flush();
                } catch (IOException e) {
                    throw new KnishRuntimeException(e.getMessage());
                }
                return KnishNull.NULL;
            });
        }

        @Override
        protected String getClassName() {
            return "System metaclass";
        }
    }

    public static class KnishString extends AbstractKnishObject {
        private final String value;

        public KnishString(String value) {
            this.value = value;

            register("toString", null, (arguments) -> this);
            register("+", 1, arguments -> {
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

    public static class KnishNull implements KnishObject {
        public final static KnishNull NULL = new KnishNull();

        private KnishNull() {
        }

        @Override
        public KnishObject call(String method, List<KnishObject> arguments) {
            throw new KnishRuntimeException("Null does not implement '" + method + "'.");
        }
    }

    public static class KnishBoolean extends AbstractKnishObject {
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

            register("toString", null, arguments -> new KnishString(Boolean.toString(value)));
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
}
