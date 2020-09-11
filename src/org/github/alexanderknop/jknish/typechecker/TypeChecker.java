package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.parser.Statement;

import java.util.List;

public class TypeChecker {
    public static void check(List<Statement> statements, ErrorReporter reporter) {
        TypeChecker checker = new TypeChecker(statements, reporter);

        checker.check();
    }

    private final List<Statement> statements;
    private final ErrorReporter reporter;

    private TypeChecker(List<Statement> statements, ErrorReporter reporter) {
        this.statements = statements;
        this.reporter = reporter;
    }

    private void check() {

    }
}
