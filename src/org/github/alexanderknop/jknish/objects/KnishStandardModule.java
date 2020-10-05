package org.github.alexanderknop.jknish.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static java.util.Collections.emptyList;

public class KnishStandardModule extends KnishModule {
    public KnishStandardModule(Writer standardOutput) {
        super();


        Class unit = this.<Writer, Void>defineClass("Unit").getInstanceClass();

        this.<Writer, Void>defineClass("System")
                .staticMethod("print",
                        List.of(anonymousClass().getter("toString", stringType())), unit,
                        (writer, arguments) -> {
                            try {
                                KnishObject string =
                                        arguments.get(0).call("toString", null);
                                if (string instanceof KnishWrappedObject<?> &&
                                        ((KnishWrappedObject<?>) string).getValue() instanceof String) {
                                    writer.write(((KnishWrappedObject<String>) string).getValue());
                                    writer.write("\n");
                                    writer.flush();
                                    return KnishCore.core().nil();
                                }
                                throw new KnishRuntimeException("toString must return a String.");
                            } catch (IOException e) {
                                throw new KnishRuntimeException(e.getMessage());
                            }
                        })
                .staticMethod("print",
                        emptyList(), unit,
                        (writer, arguments) -> {
                            try {
                                writer.write("\n");
                                writer.flush();
                            } catch (IOException e) {
                                throw new KnishRuntimeException(e.getMessage());
                            }
                            return KnishCore.core().nil();
                        })
                .staticGetter("clock",
                        KnishCore.core().numType(),
                        (writer, arguments) -> KnishCore.core().num(System.currentTimeMillis()))
                .finishDefinition(standardOutput);

    }
}
