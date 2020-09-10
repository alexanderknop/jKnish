package org.github.alexanderknop.jknish;

import java.io.IOException;
import java.io.Writer;

public final class ErrorReporter {
    private boolean errors;
    private final Writer writer;

    public ErrorReporter(Writer writer) {
        this.writer = writer;
    }

    public void error(int line, String message) {
        report(line, "", message);
    }

    public boolean hadError() {
        return errors;
    }

    private void report(int line, String where, String message) {
        try {
            writer.write(
                    "[line " + line + "] Error" + where + ": " + message + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        errors = true;
    }
}
