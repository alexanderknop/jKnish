package org.github.alexanderknop.jknish.initializationchecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.*;

import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

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

        private void defineClasses(ResolvedStatement.Block block) {
            classes.putAll(block.classes);
        }

        private void declareVariables(ResolvedStatement.Block block) {
            variableNames.putAll(block.names);
        }

        private void initialize(Integer variableId) {
            initialized.add(variableId);
        }

        private void initialize(Collection<Integer> variableIds) {
            initialized.addAll(variableIds);
        }

        private boolean isInitialized(Integer variableId) {
            return initialized.contains(variableId);
        }

        private String variableName(int variableId) {
            return variableNames.get(variableId);
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
            declareVariables(block);
            defineClasses(block);

            // the class variables are initialized by default
            initialize(block.classes.keySet());

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
            initialize(assign.variableId);

            return null;
        }

        @Override
        public Void visitCallExpression(ResolvedExpression.Call call) {
            if (isStaticCall(call)) {
                checkMethod(
                        classes.get(
                                ((ResolvedExpression.Variable) call.object).variableId
                        ).staticMethods.get(
                                new MethodId(call.method, arityFromArgumentsList(call.arguments))
                        )
                );
            } else {
                check(call.object);
            }
            if (call.arguments != null) {
                call.arguments.forEach(this::check);
            }

            return null;
        }

        private boolean isStaticCall(ResolvedExpression.Call call) {
            if (call.object instanceof ResolvedExpression.Variable) {
                ResolvedExpression.Variable variable = (ResolvedExpression.Variable) call.object;
                ResolvedStatement.Class klass = classes.get(variable.variableId);
                return klass != null &&
                        klass.staticMethods.containsKey(
                                new MethodId(
                                        call.method,
                                        arityFromArgumentsList(call.arguments)
                                )
                        );
            } else {
                return false;
            }
        }

        @Override
        public Void visitLiteralExpression(ResolvedExpression.Literal literal) {
            return null;
        }

        @Override
        public Void visitVariableExpression(ResolvedExpression.Variable variable) {
            if (!isInitialized(variable.variableId)) {
                reporter.error(variable.line, "Use of unassigned local variable '" +
                        variableName(variable.variableId) + "'.");
            }

            // if we use an object that closures on some local variables,
            // we need to check that they are initialized;
            // however, we need to avoid dead cycles
            ResolvedStatement.Class klass = classes.get(variable.variableId);
            if (klass != null && !checkedClasses.contains(klass)) {
                checkedClasses.add(klass);
                Set<Integer> initialized = new HashSet<>(this.initialized);
                checkClass(klass);
                this.initialized = initialized;
            }

            return null;
        }

        private void checkClass(ResolvedStatement.Class klass) {
            Set<Integer> initialized = this.initialized;
            Set<Integer> result = checkMethods(initialized, klass.methods.values());
            result.retainAll(checkMethods(initialized, klass.staticMethods.values()));
            result.retainAll(checkMethods(initialized, klass.constructors.values()));

            this.initialized = result;
        }

        private Set<Integer> checkMethods(Set<Integer> initialized,
                                          Collection<ResolvedStatement.Method> methods) {
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
