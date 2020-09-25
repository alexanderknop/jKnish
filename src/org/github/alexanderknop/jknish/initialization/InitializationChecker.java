package org.github.alexanderknop.jknish.initialization;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.*;

public class InitializationChecker {
    public static void check(ResolvedScript script, KnishErrorReporter reporter) {
        new InitializationCheckerVisitor(reporter).check(script);
    }


    private static class InitializationCheckerVisitor implements
            ResolvedStatement.Visitor<Void>, ResolvedExpression.Visitor<Void> {
        private final KnishErrorReporter reporter;

        private Set<Integer> initialized = new HashSet<>();
        private final Map<Integer, String> variableNames = new HashMap<>();
        private final Map<Integer, ResolvedStatement.Class> classes = new HashMap<>();
        private final Set<ResolvedStatement.Class> checkedClasses = new HashSet<>();

        private InitializationCheckerVisitor(KnishErrorReporter reporter) {
            this.reporter = reporter;

        }

        private void check(ResolvedScript script) {
            initialized.addAll(script.globals.keySet());

            check(script.code);
        }

        private void check(ResolvedExpression expression) {
            expression.accept(this);
        }

        private void check(ResolvedStatement statement) {
            statement.accept(this);
        }

        @Override
        public Void visitExpressionStatement(ResolvedStatement.Expression expression) {
            check(expression.resolvedExpression);

            return null;
        }

        @Override
        public Void visitorIfStatement(ResolvedStatement.If anIf) {
            check(anIf.condition);

            Set<Integer> initializedBefore = new HashSet<>(initialized);
            check(anIf.thenBranch);
            Set<Integer> initializedAfterThen = initialized;
            initialized = initializedBefore;

            if (anIf.elseBranch != null) {
                check(anIf.elseBranch);
            }
            initialized.retainAll(initializedAfterThen);

            return null;
        }

        @Override
        public Void visitWhileStatement(ResolvedStatement.While aWhile) {
            check(aWhile.condition);

            Set<Integer> initializedBefore = new HashSet<>(initialized);
            check(aWhile.body);

            initialized = initializedBefore;

            return null;
        }

        @Override
        public Void visitBlockStatement(ResolvedStatement.Block block) {
            variableNames.putAll(block.names);
            // the class variables are initialized by default
            initialized.addAll(block.classes.keySet());
            classes.putAll(block.classes);

            for (ResolvedStatement statement : block.resolvedStatements) {
                check(statement);
            }

            return null;
        }

        @Override
        public Void visitReturnStatement(ResolvedStatement.Return aReturn) {
            check(aReturn.value);

            return null;
        }

        @Override
        public Void visitAssignExpression(ResolvedExpression.Assign assign) {
            initialized.add(assign.variableId);

            return null;
        }

        @Override
        public Void visitCallExpression(ResolvedExpression.Call call) {
            check(call.object);
            if (call.arguments != null) {
                for (ResolvedExpression argument : call.arguments) {
                    check(argument);
                }
            }

            return null;
        }

        @Override
        public Void visitLiteralExpression(ResolvedExpression.Literal literal) {
            return null;
        }

        @Override
        public Void visitVariableExpression(ResolvedExpression.Variable variable) {
            if (!initialized.contains(variable.variableId)) {
                reporter.error(variable.line, "Use of unassigned local variable '" +
                        variableNames.get(variable.variableId) + "'.");
            }

            // if we use an object that closures on some local variables,
            // we need to check that they are initialized
            ResolvedStatement.Class klass = classes.get(variable.variableId);
            if (klass != null && !checkedClasses.contains(klass)) {
                checkedClasses.add(klass);
                checkClass(klass);
            }

            return null;
        }

        private void checkClass(ResolvedStatement.Class klass) {
            Set<Integer> initialized = this.initialized;
            Set<Integer> result = checkMethods(initialized, klass.methods);
            result.retainAll(checkMethods(initialized, klass.staticMethods));
            result.retainAll(checkMethods(initialized, klass.constructors));

            this.initialized = result;
        }

        private Set<Integer> checkMethods(Set<Integer> initialized,
                                          List<ResolvedStatement.Method> methods) {
            Set<Integer> result = null;
            for (ResolvedStatement.Method method : methods) {
                this.initialized = new HashSet<>(initialized);
                checkMethod(method);
                if (result == null) {
                    result = this.initialized;
                }
                result.retainAll(this.initialized);
            }

            return result == null ? this.initialized : result;
        }

        private void checkMethod(ResolvedStatement.Method method) {
            if (method.argumentsIds != null) {
                initialized.addAll(method.argumentsIds);
            }

            check(method.body);
        }

        @Override
        public Void visitLogicalExpression(ResolvedExpression.Logical logical) {
            Set<Integer> initializedBefore = new HashSet<>(initialized);
            check(logical.left);
            Set<Integer> initializedAfterLeft = initialized;
            initialized = initializedBefore;

            check(logical.right);
            initialized.retainAll(initializedAfterLeft);

            return null;
        }
    }
}
