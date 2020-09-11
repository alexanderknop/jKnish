package org.github.alexanderknop.jknish.interpreter.objects;

import org.github.alexanderknop.jknish.KnishObject;
import org.github.alexanderknop.jknish.interpreter.KnishRuntimeException;

import java.util.List;

public class KnishNull implements KnishObject {
    public static KnishNull NULL = new KnishNull();

    private KnishNull() {
    }

    @Override
    public KnishObject call(int line, String method, List<KnishObject> arguments) {
        throw new KnishRuntimeException(line, "Null does not implement '" + method + "'.");
    }
}
