package org.github.alexanderknop.jknish.resolver;

import org.github.alexanderknop.jknish.KnishErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;

import java.util.*;
import java.util.stream.Collectors;

public class Resolver {
    public static ResolvedScript resolve(
            KnishCore core, Statement.Block script, KnishErrorReporter reporter) {
        return new ResolverVisitor(reporter).resolve(core, script);
    }

    private static class ResolverVisitor implements
            Expression.Visitor<ResolvedExpression>, Statement.Visitor<ResolvedStatement> {
        private final KnishErrorReporter reporter;

        private final Stack<HashMap<String, Integer>> scopes = new Stack<>();
        private final Stack<HashMap<Integer, ResolvedStatement.Class>> classes = new Stack<>();
        private int currentVariable = 0;

        public ResolverVisitor(KnishErrorReporter reporter) {
            this.reporter = reporter;
        }

        private ResolvedScript resolve(KnishCore core, Statement.Block script) {
            beginScope();

            for(String objectName : core.getObjects().keySet()) {
                defineVariable(objectName);
            }

            return new ResolvedScript(visitBlockStatement(script), definedVariables());
        }

        private ResolvedExpression resolveExpression(Expression expression) {
            return expression.accept(this);
        }

        private ResolvedStatement resolveStatement(Statement statement) {
            return statement.accept(this);
        }

        private void beginScope() {
            scopes.push(new HashMap<>());
            classes.push(new HashMap<>());
        }

        private void endScope() {
            scopes.pop();
            classes.pop();
        }

        private int defineVariable(String name) {
            scopes.peek().put(name, currentVariable++);
            return currentVariable - 1;
        }

        private int variableLevel(int line, String variable) {
            for (int i = scopes.size() - 1; i >= 0; i--) {
                if (scopes.get(i).containsKey(variable)) {
                    return i;
                }
            }

            reporter.error(line, "Undeclared variable " + variable + ".");
            defineVariable(variable);
            return scopes.size() - 1;
        }

        private int variableId(int line, String variable) {
            return scopes.get(variableLevel(line, variable)).get(variable);
        }

        private boolean isClass(int line, String variable) {
            return classes.get(variableLevel(line, variable)).containsKey(variableId(line, variable));
        }

        private Map<Integer, String> definedVariables() {
            HashMap<Integer, String> names = new HashMap<>();
            scopes.peek().forEach((name, id) -> names.put(id, name));
            return Collections.unmodifiableMap(names);
        }

        private Map<Integer, ResolvedStatement.Class> definedClasses() {
            return Collections.unmodifiableMap(classes.peek());
        }

        @Override
        public ResolvedExpression visitAssignExpression(Expression.Assign assign) {
            int variableId = variableId(assign.line, assign.variable);

            if (isClass(assign.line, assign.variable)) {
                reporter.error(assign.line,
                        "Cannot assign a new value to the class variable " +
                                assign.variable + ".");
                return null;
            } else {
                return new ResolvedExpression.Assign(assign.line,
                        variableId,
                        resolveExpression(assign.value)
                );
            }
        }

        @Override
        public ResolvedExpression visitCallExpression(Expression.Call call) {
            return new ResolvedExpression.Call(call.line,
                    resolveExpression(call.object),
                    call.method,
                    call.arguments == null ? null : call.arguments.stream()
                            .map(this::resolveExpression)
                            .collect(Collectors.toList())
            );
        }

        @Override
        public ResolvedExpression visitLiteralExpression(Expression.Literal literal) {
            return new ResolvedExpression.Literal(literal.line, literal.value);
        }

        @Override
        public ResolvedExpression visitVariableExpression(Expression.Variable variable) {
            return new ResolvedExpression.Variable(variable.line,
                    variableId(variable.line, variable.name));
        }

        @Override
        public ResolvedExpression visitLogicalExpression(Expression.Logical logical) {
            return new ResolvedExpression.Logical(logical.line,
                    resolveExpression(logical.left),
                    logical.operator,
                    resolveExpression(logical.right)
            );
        }

        @Override
        public ResolvedStatement visitExpressionStatement(Statement.Expression expression) {
            return new ResolvedStatement.Expression(expression.line,
                    resolveExpression(expression.expression)
            );
        }

        @Override
        public ResolvedStatement visitorIfStatement(Statement.If anIf) {
            return new ResolvedStatement.If(anIf.line,
                    resolveExpression(anIf.condition),
                    resolveStatement(anIf.thenBranch),
                    resolveStatement(anIf.elseBranch)
            );
        }

        @Override
        public ResolvedStatement visitWhileStatement(Statement.While aWhile) {
            return new ResolvedStatement.While(aWhile.line,
                    resolveExpression(aWhile.condition),
                    resolveStatement(aWhile.body)
            );
        }

        @Override
        public ResolvedStatement visitVarStatement(Statement.Var var) {
            int variableId = defineVariable(var.name);
            if (var.initializer != null) {
                return new ResolvedStatement.Expression(var.line,
                        new ResolvedExpression.Assign(var.line,
                                variableId,
                                resolveExpression(var.initializer)
                        )
                );
            } else {
                return null;
            }
        }

        @Override
        public ResolvedStatement.Block visitBlockStatement(Statement.Block block) {
            beginScope();

            List<ResolvedStatement> resolvedStatements = new ArrayList<>();
            for (Statement statement : block.statements) {
                ResolvedStatement resolvedStatement = resolveStatement(statement);
                if (resolvedStatement != null) {
                    resolvedStatements.add(resolvedStatement);
                }
            }
            Map<Integer, String> names = definedVariables();
            Map<Integer, ResolvedStatement.Class> classes = definedClasses();

            endScope();

            return new ResolvedStatement.Block(block.line,
                    resolvedStatements,
                    names, classes
            );
        }

        @Override
        public ResolvedStatement visitClassStatement(Statement.Class klass) {
            defineVariable(klass.name);
            // todo
            return null;
        }

        @Override
        public ResolvedStatement visitReturnStatement(Statement.Return aReturn) {
            return new ResolvedStatement.Return(aReturn.line,
                    resolveExpression(aReturn.value)
            );
        }
    }
}
