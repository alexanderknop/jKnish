package org.github.alexanderknop.jknish.initializationchecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static org.github.alexanderknop.jknish.parser.MethodId.arityFromArgumentsList;

public class InitializationChecker {
    public static void check(ResolvedScript script, KnishErrorReporter reporter) {
        new InitializationCheckerVisitor(reporter).check(script);
    }


    private static class InitializationCheckerVisitor implements
            ResolvedStatement.Visitor<Void>, ResolvedExpression.Visitor<Void> {
        private final KnishErrorReporter reporter;

        private final Map<Integer, String> variableNames = new HashMap<>();
        private final Map<Integer, ResolvedStatement.Class> classes = new HashMap<>();
        private final Map<ResolvedStatement.Class, Map<ResolvedStatement.Method, BitSet>>
                beforeMethod = new HashMap<>();
        private final Map<ResolvedStatement.Class, Map<ResolvedStatement.Method, BitSet>>
                afterMethod = new HashMap<>();

        private int thisId = -1;
        private ResolvedStatement.Class thisClass;

        private BitSet initialized;
        private Map<MethodId, ResolvedStatement.Method> thisContext;

        private InitializationCheckerVisitor(KnishErrorReporter reporter) {
            this.reporter = reporter;

        }

        private void check(ResolvedScript script) {
            initialized = new BitSet();

            script.globals.keySet().forEach(id -> initialized.set(id));

            check(script.code);
        }

        private void check(ResolvedExpression expression) {
            expression.accept(this);
        }

        private void check(ResolvedStatement statement) {
            statement.accept(this);
        }

        private void checkClass(ResolvedStatement.Class klass) {
            BitSet initialized = (BitSet) this.initialized.clone();
            int previousThisId = thisId;
            ResolvedStatement.Class previousThisClass = thisClass;
            Map<MethodId, ResolvedStatement.Method> previousThisContext = thisContext;

            thisClass = klass;

            thisId = klass.staticThisId;
            thisContext = new HashMap<>(klass.staticMethods);
            thisContext.putAll(klass.constructors);
            klass.staticMethods.forEach(
                    (id, method) -> {
                        checkMethod(klass, method);
                        this.initialized = (BitSet) initialized.clone();
                    }
            );
            klass.constructors.forEach(
                    (id, method) -> {
                        checkMethod(klass, method);
                        this.initialized = (BitSet) initialized.clone();
                    }
            );

            thisId = klass.thisId;
            thisContext = klass.methods;
            klass.methods.forEach(
                    (id, method) -> {
                        checkMethod(klass, method);
                        this.initialized = (BitSet) initialized.clone();
                    }
            );

            thisId = previousThisId;
            thisClass = previousThisClass;
            thisContext = previousThisContext;
        }

        private BitSet setBefore(ResolvedStatement.Class klass,
                                 ResolvedStatement.Method method,
                                 BitSet newBefore) {
            if (!beforeMethod.containsKey(klass)) {
                beforeMethod.put(klass, new HashMap<>());
                afterMethod.put(klass, new HashMap<>());
            }

            if (!beforeMethod.get(klass).containsKey(method)) {
                beforeMethod.get(klass).put(method, newBefore);
                afterMethod.get(klass).put(method, newBefore);
            } else {
                beforeMethod.get(klass).get(method).and(newBefore);
            }

            return beforeMethod.get(klass).get(method);
        }

        private boolean hasBeforeChanged(ResolvedStatement.Class klass,
                                         ResolvedStatement.Method method,
                                         BitSet newBefore) {
            return !(beforeMethod.containsKey(klass) &&
                    beforeMethod.get(klass).containsKey(method) &&
                    beforeMethod.get(klass).get(method).equals(newBefore));
        }

        private void checkMethod(ResolvedStatement.Class klass,
                                 ResolvedStatement.Method method) {
            if (hasBeforeChanged(klass, method, initialized)) {
                this.initialized = setBefore(klass, method, initialized);
                if (method.argumentsIds != null) {
                    method.argumentsIds.forEach(initialized::set);
                }
                method.argumentNames.forEach(variableNames::put);

                check(method.body);
                afterMethod.get(klass).put(method, this.initialized);
            } else {
                this.initialized = (BitSet) afterMethod.get(klass).get(method).clone();
            }
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
        public Void visitAssignExpression(ResolvedExpression.Assign assign) {
            check(assign.value);
            initialized.set(assign.variableId);

            return null;
        }

        @Override
        public Void visitCallExpression(ResolvedExpression.Call call) {
            if (isThisCall(call)) {
                MethodId methodId =
                        new MethodId(call.method, arityFromArgumentsList(call.arguments));
                checkMethod(thisClass, thisContext.get(methodId));
            } else if (isStaticCall(call)) {
                MethodId methodId =
                        new MethodId(call.method, arityFromArgumentsList(call.arguments));
                ResolvedStatement.Class klass =
                        classes.get(((ResolvedExpression.Variable) call.object).variableId);
                thisId = klass.staticThisId;
                thisContext = klass.staticMethods;
                thisClass = klass;
                checkMethod(klass, klass.staticMethods.get(methodId));
            } else {
                check(call.object);
                if (call.arguments != null) {
                    call.arguments.forEach(this::check);
                }
            }
            return null;
        }

        private boolean isThisCall(ResolvedExpression.Call call) {
            return call.object instanceof ResolvedExpression.Variable &&
                    ((ResolvedExpression.Variable) call.object).variableId == thisId;
        }

        @Override
        public Void visitLiteralExpression(ResolvedExpression.Literal literal) {
            return null;
        }

        @Override
        public Void visitVariableExpression(ResolvedExpression.Variable variable) {
            if (thisId == variable.variableId) {
                checkClass(thisClass);
            } else if (!initialized.get(variable.variableId)) {
                reporter.error(variable.line,
                        "Use of unassigned local variable '" +
                                variableNames.get(variable.variableId) + "'.");

            } else if (classes.containsKey(variable.variableId)) {
                checkClass(classes.get(variable.variableId));
            }

            return null;
        }

        @Override
        public Void visitLogicalExpression(ResolvedExpression.Logical logical) {
            check(logical.left);
            BitSet initialized = (BitSet) this.initialized.clone();
            check(logical.right);
            this.initialized = initialized;

            return null;
        }

        @Override
        public Void visitExpressionStatement(ResolvedStatement.Expression expression) {
            check(expression.resolvedExpression);

            return null;
        }

        @Override
        public Void visitorIfStatement(ResolvedStatement.If anIf) {
            check(anIf.condition);
            BitSet initializedAfterCondition = (BitSet) this.initialized.clone();

            check(anIf.thenBranch);

            // the variable is initialized after if only if it is initialized in
            // both branches
            if (anIf.elseBranch != null) {
                BitSet initializedAfterThen = this.initialized;
                this.initialized = (BitSet) initializedAfterCondition.clone();
                check(anIf.elseBranch);
                this.initialized.and(initializedAfterThen);
            } else {
                this.initialized = initializedAfterCondition;
            }

            return null;
        }

        @Override
        public Void visitWhileStatement(ResolvedStatement.While aWhile) {
            check(aWhile.condition);
            BitSet initialized = (BitSet) this.initialized.clone();
            check(aWhile.body);
            this.initialized = initialized;

            return null;
        }

        @Override
        public Void visitBlockStatement(ResolvedStatement.Block block) {
            // class variables are initialized by default
            block.classes.keySet().forEach(initialized::set);
            // store class declarations
            block.classes.forEach(classes::put);
            // store variable names
            block.names.forEach(variableNames::put);

            block.resolvedStatements.forEach(this::check);

            return null;
        }

        @Override
        public Void visitReturnStatement(ResolvedStatement.Return aReturn) {
            if (aReturn.value != null) {
                check(aReturn.value);
            }
            return null;
        }
    }
}
