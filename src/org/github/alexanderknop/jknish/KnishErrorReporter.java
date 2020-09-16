package org.github.alexanderknop.jknish;

import org.github.alexanderknop.jknish.scanner.Token;
import org.github.alexanderknop.jknish.scanner.TokenType;

import java.io.IOException;
import java.io.Writer;

public final class KnishErrorReporter {
    private boolean errors;
    private final Writer writer;

    public KnishErrorReporter(Writer writer) {
        this.writer = writer;
    }

    public void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
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
