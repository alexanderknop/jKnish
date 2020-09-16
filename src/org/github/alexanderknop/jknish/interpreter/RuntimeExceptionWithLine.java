package org.github.alexanderknop.jknish.interpreter;

import org.github.alexanderknop.jknish.objects.KnishRuntimeException;

class RuntimeExceptionWithLine extends RuntimeException {
    private final int line;

    public RuntimeExceptionWithLine(int line, KnishRuntimeException exception) {
        super(exception.getMessage());
        this.line = line;
    }

    public RuntimeExceptionWithLine(int line, String message) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}
