package org.github.alexanderknop.jknish.returnchecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

public class ReturnCheck {

    public static void check(ResolvedScript script, KnishErrorReporter reporter) {
        new ReturnCheckVisitor(reporter).check(script);
    }

    private static class ReturnCheckVisitor implements
            ResolvedStatement.Visitor<Boolean> {

        private final KnishErrorReporter reporter;

        private ReturnType returnType = ReturnType.NOT_FIXED;
        private boolean inMethod = false;

        public void check(ResolvedScript script) {
            visitBlockStatement(script.code);
        }

        private Boolean check(ResolvedStatement statement) {
            if (statement != null) {
                return statement.accept(this);
            }
            return Boolean.FALSE;
        }

        public ReturnCheckVisitor(KnishErrorReporter reporter) {
            this.reporter = reporter;
        }

        private void setReturnType(int line, ReturnType returnType) {
            if (this.returnType == ReturnType.NOT_FIXED) {
                this.returnType = returnType;
            } else if (this.returnType != returnType) {
                reporter.error(line,
                        "Cannot return nothing and some value in the same method.");
            }
        }

        @Override
        public Boolean visitExpressionStatement(ResolvedStatement.Expression expression) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visitorIfStatement(ResolvedStatement.If anIf) {
            Boolean returnInThen = check(anIf.thenBranch);
            Boolean returnInElse = check(anIf.elseBranch);
            return returnInThen && returnInElse;
        }

        @Override
        public Boolean visitWhileStatement(ResolvedStatement.While aWhile) {
            return check(aWhile.body);
        }

        @Override
        public Boolean visitBlockStatement(ResolvedStatement.Block block) {
            Boolean alwaysReturn = Boolean.FALSE;
            boolean reported = false;
            for (ResolvedStatement statement : block.resolvedStatements) {
                if (alwaysReturn && !reported) {
                    reporter.error(statement.line, "Unreachable statement.");
                    reported = true;
                }

                if (check(statement)) {
                    alwaysReturn = Boolean.TRUE;
                }
            }

            for (ResolvedStatement.Class klass : block.classes.values()) {
                klass.methods.forEach(this::checkMethod);
                klass.staticMethods.forEach(this::checkMethod);
                klass.constructors.forEach(this::checkMethod);
            }

            return alwaysReturn;
        }

        private void checkMethod(ResolvedStatement.Method method) {
            ReturnType previousReturnType = returnType;
            returnType = ReturnType.NOT_FIXED;
            boolean previousInMethod = inMethod;
            inMethod = true;

            check(method.body);

            returnType = previousReturnType;
            inMethod = previousInMethod;
        }

        @Override
        public Boolean visitReturnStatement(ResolvedStatement.Return aReturn) {
            if (!inMethod) {
                reporter.error(aReturn.line, "Cannot return from top-level code.");
            } else {
                setReturnType(aReturn.line,
                        aReturn.value == null ? ReturnType.EMPTY : ReturnType.WITH_VALUE);
            }
            return Boolean.TRUE;
        }

        private enum ReturnType {
            EMPTY, WITH_VALUE, NOT_FIXED
        }
    }
}
