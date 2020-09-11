package org.github.alexanderknop.jknish.interpreter;

public class KnishRuntimeException extends RuntimeException {
    public final int line;
    public final String message;

    public KnishRuntimeException(int line, String message) {
        this.line = line;
        this.message = message;
    }
}
