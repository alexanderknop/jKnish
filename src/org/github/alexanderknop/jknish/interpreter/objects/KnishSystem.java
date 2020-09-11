package org.github.alexanderknop.jknish.interpreter.objects;

import org.github.alexanderknop.jknish.KnishObject;
import org.github.alexanderknop.jknish.interpreter.KnishRuntimeException;

import java.io.IOException;
import java.io.Writer;

public class KnishSystem extends AbstractKnishObject {
    public KnishSystem(Writer output) {
        register("print", 1, (line, arguments) -> {
            try {
                KnishObject string =
                        arguments.get(0).call(line, "toString", null);
                if (!(string instanceof KnishString)) {
                    throw new KnishRuntimeException(line, "toString must return a String.");
                }
                output.write(string.toString());
                output.write("\n");
                output.flush();
            } catch (IOException e) {
                throw new KnishRuntimeException(line, e.getMessage());
            }
            return KnishNull.NULL;
        });
    }

    @Override
    protected String getClassName() {
        return "System metaclass";
    }
}
