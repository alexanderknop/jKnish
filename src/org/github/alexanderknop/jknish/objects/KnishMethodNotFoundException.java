package org.github.alexanderknop.jknish.objects;

import org.github.alexanderknop.jknish.parser.MethodId;

public class KnishMethodNotFoundException extends KnishRuntimeException {
    public KnishMethodNotFoundException(String className, MethodId methodId) {
        super(className + " does not implement '" + methodId + "'.");
    }
}
