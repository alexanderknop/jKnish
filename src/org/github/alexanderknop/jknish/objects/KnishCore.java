package org.github.alexanderknop.jknish.objects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnishCore extends KnishModule {

    private final ClassDefinition<Void, Long> numMeta;
    private final ClassDefinition<Void, Boolean> boolMeta;
    private final ClassDefinition<Void, String> stringMeta;

    private final Map<Boolean, KnishObject> booleanValues = new HashMap<>();

    private final static KnishCore CORE = new KnishCore();
    private final KnishObject nullObject;

    public static KnishCore core() {
        return CORE;
    }

    private KnishCore() {
        super();

        // the order of class definitions is important since
        // super uses boolean type to define ===
        boolMeta = this.defineClass("Bool");
        numMeta = this.defineClass("Num");
        stringMeta = this.defineClass("String");

        nullObject = this.defineClass("Null").construct(null);

        Class num = numMeta.getInstanceClass();
        Class str = stringMeta.getInstanceClass();
        Class bool = boolMeta.getInstanceClass();

        boolMeta
                .getter("!",
                        bool,
                        (value, arguments) -> bool(!value))
                .getter("toString",
                        str,
                        (value, arguments) -> str(Boolean.toString(value)))
                .finishDefinition(null);

        stringMeta
                .getter("toString",
                        str,
                        (value, argument) -> str(value))
                .method("+",
                        List.of(str), str,
                        (value, arguments) -> {
                            String argumentValue =
                                    KnishWrappedObject.unwrap(arguments.get(0), String.class,
                                            "Argument must be a wrapped String.");
                            return stringMeta.construct(value + argumentValue);
                        });

        numMeta.staticMethod("fromString",
                List.of(str), num,
                (ignored, arguments) -> {
                    String value = KnishWrappedObject.unwrap(arguments.get(0), String.class,
                            "Argument must be a wrapped String.");
                    return num(Long.parseLong(value));
                })
                .getter("toString", str,
                        (value, arguments) -> stringMeta.construct(Long.toString(value)))
                .method("+",
                        List.of(num), num,
                        (value, arguments) -> {
                            Long argumentValue =
                                    KnishWrappedObject.unwrap(arguments.get(0), Long.class,
                                            "Argument must be a wrapped Long.");
                            return numMeta.construct(value + argumentValue);
                        })
                .method("-",
                        List.of(num), num,
                        (value, arguments) -> {
                            Long argumentValue =
                                    KnishWrappedObject.unwrap(arguments.get(0), Long.class,
                                            "Argument must be a wrapped Long.");
                            return numMeta.construct(value - argumentValue);
                        })
                .getter("-",
                        num,
                        (value, arguments) -> numMeta.construct(-value))
                .method("*",
                        List.of(num), num,
                        (value, arguments) -> {
                            Long argumentValue =
                                    KnishWrappedObject.unwrap(arguments.get(0), Long.class,
                                            "Argument must be a wrapped Long.");
                            return numMeta.construct(value * argumentValue);
                        })
                .method("/",
                        List.of(num), num,
                        (value, arguments) -> {
                            Long argumentValue =
                                    ((KnishWrappedObject<Long>) arguments.get(0)).getValue();
                            return numMeta.construct(value / argumentValue);
                        })
                .method("<",
                        List.of(num), bool,
                        (value, arguments) -> {
                            Long argumentValue =
                                    KnishWrappedObject.unwrap(arguments.get(0), Long.class,
                                            "Argument must be a wrapped Long.");
                            return bool(value < argumentValue);
                        })
                .method(">",
                        List.of(num), bool,
                        (value, arguments) -> {
                            Long argumentValue =
                                    ((KnishWrappedObject<Long>) arguments.get(0)).getValue();
                            return bool(value > argumentValue);
                        })
                .method("<=",
                        List.of(num), bool,
                        (value, arguments) -> {
                            Long argumentValue =
                                    KnishWrappedObject.unwrap(arguments.get(0), Long.class,
                                            "Argument must be a wrapped Long.");
                            return bool(value <= argumentValue);
                        })
                .method(">=",
                        List.of(num), bool,
                        (value, arguments) -> {
                            Long argumentValue =
                                    KnishWrappedObject.unwrap(arguments.get(0), Long.class,
                                            "Argument must be a wrapped Long.");
                            return bool(value >= argumentValue);
                        })
                .finishDefinition(null);

        booleanValues.put(true, boolMeta.construct(true));
        booleanValues.put(false, boolMeta.construct(false));
    }

    public KnishObject num(long value) {
        return numMeta.construct(value);
    }

    public KnishObject str(String value) {
        return stringMeta.construct(value);
    }

    public KnishObject bool(boolean value) {
        return booleanValues.get(value);
    }

    public KnishObject nil() {
        return nullObject;
    }
}
