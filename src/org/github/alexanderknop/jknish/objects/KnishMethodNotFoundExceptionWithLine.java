package org.github.alexanderknop.jknish.objects;

public class KnishMethodNotFoundExceptionWithLine extends KnishRuntimeException {
    public KnishMethodNotFoundExceptionWithLine(int line,
                                                String className, String method, Integer argumentsCount) {
        super(className + " does not implement '" + method +
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
