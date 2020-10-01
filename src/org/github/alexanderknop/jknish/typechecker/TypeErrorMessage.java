package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;

class TypeErrorMessage {
    private final KnishErrorReporter reporter;
    private final int line;
    private final String message;
    private boolean sent = false;

    TypeErrorMessage(KnishErrorReporter reporter, int line, String message) {
        this.reporter = reporter;
        this.line = line;
        this.message = message;
    }

    public void send() {
        if (!sent) {
            reporter.error(line, message);
            sent = true;
        }
    }
}
