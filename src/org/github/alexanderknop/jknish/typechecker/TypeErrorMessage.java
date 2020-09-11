package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.ErrorReporter;

class TypeErrorMessage {
    private final ErrorReporter reporter;
    private final int line;
    private final String message;

    TypeErrorMessage(ErrorReporter reporter, int line, String message) {
        this.reporter = reporter;
        this.line = line;
        this.message = message;
    }

    public void send() {
        reporter.error(line, message);
    }
}
