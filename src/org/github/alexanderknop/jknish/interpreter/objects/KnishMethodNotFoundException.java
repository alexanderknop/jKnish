package org.github.alexanderknop.jknish.interpreter.objects;

import org.github.alexanderknop.jknish.interpreter.KnishRuntimeException;

public class KnishMethodNotFoundException extends KnishRuntimeException {
    public KnishMethodNotFoundException(int line,
                                        String className, String method, Integer argumentsCount) {
        super(line, className + " does not implement '" + method +
                argumentCountToString(argumentsCount) + "'.");
    }

    private static String argumentCountToString(Integer argumentsCount) {
        String arguments;
        if (argumentsCount == null) {
            arguments = "";
        } else if (argumentsCount == 1) {
            arguments = "(_)";
        } else {
            arguments = "_, ".repeat(argumentsCount);
            arguments = "(" + arguments.substring(0, arguments.length() - 2) + ")";
        }
        return arguments;
    }
}
