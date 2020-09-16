package org.github.alexanderknop.jknish.objects;

import org.github.alexanderknop.jknish.parser.MethodId;

class MethodNotFoundException extends KnishRuntimeException {
    public MethodNotFoundException(String className, MethodId methodId) {
        super(className + " does not implement '" + methodId + "'.");
    }
}
