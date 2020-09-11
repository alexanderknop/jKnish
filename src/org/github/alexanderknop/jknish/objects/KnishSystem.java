package org.github.alexanderknop.jknish.objects;

import java.io.IOException;
import java.io.Writer;

public class KnishSystem extends AbstractKnishObject {
    public KnishSystem(Writer output) {
        register("print", 1, (line, arguments) -> {
            try {
                KnishObject string =
                        arguments.get(0).call(line, "toString", null);
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
