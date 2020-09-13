package org.github.alexanderknop.jknish.objects;

public class KnishMethodNotFoundException extends KnishRuntimeException {
    public KnishMethodNotFoundException(String className, MethodId methodId) {
        super(className + " does not implement '" + methodId + "'.");
    }
}
