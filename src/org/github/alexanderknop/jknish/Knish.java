package org.github.alexanderknop.jknish;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Knish {
    public static void run(
            String source,
            Writer output,
            ErrorReporter reporter) {
        // TODO
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
