package org.github.alexanderknop.jknish.initializationchecker;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.parser.MethodId;
import org.github.alexanderknop.jknish.resolver.ResolvedExpression;
import org.github.alexanderknop.jknish.resolver.ResolvedScript;
import org.github.alexanderknop.jknish.resolver.ResolvedStatement;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class InitializationChecker {
    public static void check(ResolvedScript script, KnishErrorReporter reporter) {
        new InitializationCheckerVisitor(reporter).check(script);
    }


    private static class InitializationCheckerVisitor implements
            ResolvedStatement.Visitor<Void>, ResolvedExpression.Visitor<Void> {
        private final KnishErrorReporter reporter;

        private final Map<Integer, String> variableNames = new HashMap<>();

        private final Map<Integer, ResolvedStatement.Class> localObjects =
                new HashMap<>();
        private final Map<Integer, Map<MethodId, ResolvedStatement.Method>>
                localFunctions = new HashMap<>();

        private final Map<ResolvedStatement.Class, Map<ResolvedStatement.Method, BitSet>>
                beforeMethod = new HashMap<>();
        private final Map<ResolvedStatement.Class, Map<ResolvedStatement.Method, BitSet>>
                afterMethod = new HashMap<>();

        private BitSet initialized;

        private InitializationCheckerVisitor(KnishErrorReporter reporter) {
            this.reporter = reporter;

        }

        private boolean isLocalCall(ResolvedExpression.Call call) {
            if (call.object instanceof ResolvedExpression.Variable) {
                ResolvedExpression.Variable variable =
                        ((ResolvedExpression.Variable) call.object);
                if (localObjects.containsKey(variable.variableId)) {
                    MethodId methodId =
                            new MethodId(
                                    call.method,
                                    MethodId.arityFromArgumentsList(call.arguments)
                            );
                    return localFunctions.get(variable.variableId).containsKey(methodId);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        private boolean isLocalObject(int variableId) {
            return localObjects.containsKey(variableId);
        }

        private ResolvedStatement.Class classOfLocalObject(int variableId) {
            return localObjects.get(variableId);
        }

        private void checkLocalCall(ResolvedExpression.Call call) {
            ResolvedExpression.Variable localVariable =
                    (ResolvedExpression.Variable) call.object;

            ResolvedStatement.Class klass = classOfLocalObject(localVariable.variableId);
            ResolvedStatement.Method method = localFunctions.get(localVariable.variableId).get(
                    new MethodId(call.method, MethodId.arityFromArgumentsList(call.arguments))
            );

            checkMethod(klass, method);
        }

        private void check(ResolvedScript script) {
            initialized = new BitSet();

            script.globals.keySet().forEach(id -> initialized.set(id));

            visitBlockStatement(script.code);
        }

        private void check(ResolvedExpression expression) {
            expression.accept(this);
        }

        private void check(ResolvedStatement statement) {
            statement.accept(this);
        }

        private void checkClass(ResolvedStatement.Class klass) {
            BitSet initialized = (BitSet) this.initialized.clone();

            localObjects.put(klass.staticThisId, klass);
            localFunctions.put(klass.staticThisId, new HashMap<>());
            klass.staticMethods.forEach(localFunctions.get(klass.staticThisId)::put);
            klass.staticFields.keySet().forEach(initialized::set);
            klass.staticMethods.forEach(
                    (id, method) -> {
                        checkMethod(klass, method);
                        this.initialized = (BitSet) initialized.clone();
                    }
            );
            localObjects.remove(klass.staticThisId);

            localObjects.put(klass.thisId, klass);
            localFunctions.put(klass.thisId, new HashMap<>());
            klass.methods.forEach(localFunctions.get(klass.thisId)::put);
            klass.fields.keySet().forEach(initialized::set);
            klass.constructors.forEach(
                    (id, method) -> {
                        checkMethod(klass, method);
                        this.initialized = (BitSet) initialized.clone();
                    }
            );
            klass.methods.forEach(
                    (id, method) -> {
                        checkMethod(klass, method);
                        this.initialized = (BitSet) initialized.clone();
                    }
            );
            localObjects.remove(klass.thisId);
            localFunctions.remove(klass.thisId);
        }

        private BitSet setBefore(ResolvedStatement.Class klass,
                                 ResolvedStatement.Method method,
                                 BitSet newBefore) {
            if (!beforeMethod.containsKey(klass)) {
                beforeMethod.put(klass, new HashMap<>());
                afterMethod.put(klass, new HashMap<>());
            }

            if (!beforeMethod.get(klass).containsKey(method)) {
                beforeMethod.get(klass).put(method, (BitSet) newBefore.clone());
                afterMethod.get(klass).put(method, (BitSet) newBefore.clone());
            } else {
                beforeMethod.get(klass).get(method).and(newBefore);
            }

            return (BitSet) beforeMethod.get(klass).get(method).clone();
        }

        private static boolean isSubset(BitSet left, BitSet right) {
            BitSet copyOfLeft = (BitSet) left.clone();
            copyOfLeft.and(right);
            return left.equals(left);
        }

        private boolean hasBeforeChanged(ResolvedStatement.Class klass,
                                         ResolvedStatement.Method method,
                                         BitSet newBefore) {
            return !(beforeMethod.containsKey(klass) &&
                    beforeMethod.get(klass).containsKey(method) &&
                    isSubset(beforeMethod.get(klass).get(method), newBefore));
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

        @Override
        public Void visitAssignExpression(ResolvedExpression.Assign assign) {
            check(assign.value);
            initialized.set(assign.variableId);

            return null;
        }

        @Override
        public Void visitCallExpression(ResolvedExpression.Call call) {
            if (isLocalCall(call)) {
                // if this is a local call, we may go into the method to see
                // what variables it initializes
                checkLocalCall(call);
            } else {
                check(call.object);
            }
            // check that the arguments are not using not initialized variables
            if (call.arguments != null) {
                call.arguments.forEach(this::check);
            }

            return null;
        }

        @Override
        public Void visitLiteralExpression(ResolvedExpression.Literal literal) {
            return null;
        }

        @Override
        public Void visitVariableExpression(ResolvedExpression.Variable variable) {
            if (isLocalObject(variable.variableId)) {
                checkClass(classOfLocalObject(variable.variableId));
            } else if (!initialized.get(variable.variableId)) {
                reporter.error(variable.line,
                        "Use of unassigned local variable '" +
                                variableNames.get(variable.variableId) + "'.");

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
            // store variable names
            block.names.forEach(variableNames::put);
            // all meta classes are local object
            block.classes.forEach(localObjects::put);
            // make all static functions local
            block.classes.forEach((id, klass) -> {
                localFunctions.put(id, new HashMap<>());
                klass.staticMethods.forEach(localFunctions.get(id)::put);
            });
            block.classes.forEach((id, klass) -> {
                int thisId = klass.staticThisId;
                localObjects.put(thisId, klass);
                localFunctions.put(thisId, new HashMap<>());
                klass.staticMethods.forEach(localFunctions.get(thisId)::put);
                klass.staticFields.keySet().forEach(initialized::set);
            });

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
