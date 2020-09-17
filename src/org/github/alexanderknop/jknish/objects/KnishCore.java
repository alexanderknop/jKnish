package org.github.alexanderknop.jknish.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;

public class KnishCore extends KnishModule {
    public KnishCore(Writer output) {
        Class str = declareClass("String");
        Class num = declareClass("Number");
        Class bool = declareClass("Boolean");
        Class unit = declareClass("Unit");

        bool.getter("toString", str)
                .getter("!", bool);

        str.getter("toString", str)
                .method("+", List.of(str), str);

        num.getter("toString", str)
                .getter("-", num)
                .method("+", List.of(num), num)
                .method("-", List.of(num), num)
                .method("*", List.of(num), num)
                .method("/", List.of(num), num)
                .method(">", List.of(num), bool)
                .method("<", List.of(num), bool)
                .method(">=", List.of(num), bool)
                .method("<=", List.of(num), bool);

        Class systemMetaclass =
                declareClass("System metaclass")
                        .method("print",
                                List.of(anonymousClass().getter("toString", str)), unit)
                        .getter("clock", num);

        define(
                "System",
                KnishWrappedObject.<Writer>object("System metaclass")
                        .method("print", 1,
                                (writer, arguments) -> {
                                    try {
                                        KnishObject string =
                                                arguments.get(0).call("toString", null);
                                        if (!(string instanceof KnishString)) {
                                            throw new KnishRuntimeException("toString must return a String.");
                                        }
                                        writer.write(string.toString());
                                        writer.write("\n");
                                        writer.flush();
                                    } catch (IOException e) {
                                        throw new KnishRuntimeException(e.getMessage());
                                    }
                                    return KnishNull.NULL;
                                })
                        .getter("clock", (writer, arguments) -> num(System.currentTimeMillis()))
                        .construct(output),
                systemMetaclass
        );
    }

    public static KnishObject num(long value) {
        return new KnishNumber(value);
    }

    public static KnishObject str(String value) {
        return new KnishString(value);
    }

    public static KnishObject bool(boolean value) {
        return new KnishBoolean(value);
    }

    public static KnishObject nil() {
        return KnishNull.NULL;
    }

    public static class KnishNumber extends AbstractKnishObject {
        private final Long value;

        private KnishNumber(Long value) {
            this.value = value;

            register("toString", null, (arguments) -> new KnishString(Long.toString(value)));
            register("+", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return num(value + right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("-", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return num(value - right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("-", null, arguments -> num(-value));
            register("*", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return num(value * right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("/", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return num(value / right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });

            register("<", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return bool(value < right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register(">", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return bool(value > right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("<=", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return bool(value <= right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register(">=", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return bool(value >= right.value);
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("==", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return bool(value.equals(right.value));
                } else {
                    throw new KnishRuntimeException("Right operand must be a number.");
                }
            });
            register("!=", 1, arguments -> {
                if (arguments.get(0) instanceof KnishNumber) {
                    KnishNumber right = (KnishNumber) arguments.get(0);
                    return bool(!value.equals(right.value));
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

    public static class KnishString extends AbstractKnishObject {
        private final String value;

        private KnishString(String value) {
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
        private final static KnishNull NULL = new KnishNull();

        private KnishNull() {
        }

        @Override
        public KnishObject call(String method, List<KnishObject> arguments) {
            throw new KnishRuntimeException("Nil does not implement '" + method + "'.");
        }
    }

    public static class KnishBoolean extends AbstractKnishObject {
        public static final KnishBoolean TRUE = new KnishBoolean(true);
        public static final KnishBoolean FALSE = new KnishBoolean(false);

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
