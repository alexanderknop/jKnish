package org.github.alexanderknop.jknish.objects;

import java.util.List;

public class KnishNull implements KnishObject {
    public final static KnishNull NULL = new KnishNull();

    private KnishNull() {
    }

    @Override
    public KnishObject call(int line, String method, List<KnishObject> arguments) {
        throw new KnishRuntimeException("Null does not implement '" + method + "'.");
    }
}
