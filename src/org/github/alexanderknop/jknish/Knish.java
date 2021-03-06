package org.github.alexanderknop.jknish;

import org.github.alexanderknop.jknish.initializationchecker.InitializationChecker;
import org.github.alexanderknop.jknish.interpreter.Interpreter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishStandardModule;
import org.github.alexanderknop.jknish.parser.Parser;
import org.github.alexanderknop.jknish.parser.Statement;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.Resolver;
import org.github.alexanderknop.jknish.returnchecker.ReturnChecker;
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
            KnishErrorReporter reporter) {

        KnishCore core = KnishCore.core();

        List<Token> tokens = Scanner.tokens(source, reporter);
        if (reporter.hadError()) {
            return;
        }


        Statement.Block script = Parser.parse(tokens, reporter);
        if (reporter.hadError()) {
            return;
        }

        KnishStandardModule standardModule = new KnishStandardModule(output);

        ResolvedScript resolvedScript = Resolver.resolve(script, reporter, standardModule);

        InitializationChecker.check(resolvedScript, reporter);
        ReturnChecker.check(resolvedScript, reporter);

        TypeChecker.check(resolvedScript, reporter, standardModule);
        if (reporter.hadError()) {
            return;
        }

        Interpreter.interpret(resolvedScript, reporter, standardModule);
    }

    private static void runFile(String path) throws IOException {
        KnishErrorReporter reporter = new KnishErrorReporter(new OutputStreamWriter(System.err));

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
