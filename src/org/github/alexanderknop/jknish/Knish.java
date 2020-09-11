package org.github.alexanderknop.jknish;

import org.github.alexanderknop.jknish.interpreter.Interpreter;
import org.github.alexanderknop.jknish.parser.Parser;
import org.github.alexanderknop.jknish.parser.Statement;
import org.github.alexanderknop.jknish.scanner.Scanner;
import org.github.alexanderknop.jknish.scanner.Token;
import org.github.alexanderknop.jknish.typechecker.TypeChecker;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Knish {
    public static void run(
            String source,
            Writer output,
            ErrorReporter reporter) {
        List<Token> tokens = Scanner.tokens(source, reporter);
        if (reporter.hadError()) {
            return;
        }

        List<Statement> statements = Parser.parse(tokens, reporter);
        if (reporter.hadError()) {
            return;
        }

        TypeChecker.check(statements, reporter);
        if (reporter.hadError()) {
            return;
        }

        Interpreter.interpret(statements, output, reporter);
    }

    private static void runFile(String path) throws IOException {
        ErrorReporter reporter = new ErrorReporter(new OutputStreamWriter(System.err));

        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()),
                new OutputStreamWriter(System.out),
                reporter);

        if (reporter.hadError()) {
            System.exit(65);
        }
    }


    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: jknish [script]");
            System.exit(64);
        } else {
            runFile(args[0]);
        }
    }
}
