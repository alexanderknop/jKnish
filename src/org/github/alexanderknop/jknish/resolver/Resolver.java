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

        private final Stack<Map<String, VariableInformation>> scopes = new Stack<>();
        private final Stack<Map<Integer, ResolvedStatement.Class>> classes = new Stack<>();
        private int currentVariable = 0;

        public ResolverVisitor(KnishErrorReporter reporter) {
            this.reporter = reporter;
        }

        private ResolvedScript resolve(KnishCore core, Statement.Block script) {
            beginScope();

            for (String objectName : core.getObjects().keySet()) {
                defineVariable(0, objectName);
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
            this.scopes.push(new HashMap<>());
            this.classes.push(new HashMap<>());
        }

        private void endScope() {
            scopes.pop();
            classes.pop();
        }

        private void reportUnused() {
            scopes.peek().forEach((name, information) -> {
                if (!information.used) {
                    if (information.isClass) {
                        reporter.error(information.line, "The class " +
                                name + " is defined, but never used.");
                    } else {
                        reporter.error(information.line, "The variable " +
                                name + " is defined, but never used.");
                    }
                }
            });
        }

        private int defineVariable(int line, String name) {
            return defineVariable(line, name, false);
        }

        private int defineVariable(int line, String name, boolean isClass) {
            scopes.peek().put(name, new VariableInformation(line, currentVariable++, isClass));
            return currentVariable - 1;
        }

        private VariableInformation variableInformation(int line, String variable) {
            for (int i = scopes.size() - 1; i >= 0; i--) {
                if (scopes.get(i).containsKey(variable)) {
                    return scopes.get(i).get(variable);
                }
            }

            reporter.error(line, "Undeclared variable " + variable + ".");
            defineVariable(line, variable);
            VariableInformation variableInformation = scopes.peek().get(variable);
            variableInformation.used = true;
            return variableInformation;
        }

        private int variableId(int line, String variable) {
            return variableInformation(line, variable).id;
        }

        private void useVariable(int line, String variable) {
            variableInformation(line, variable).used = true;
        }

        private boolean isClass(int line, String variable) {
            return variableInformation(line, variable).isClass;
        }

        private Map<Integer, String> definedVariables() {
            HashMap<Integer, String> names = new HashMap<>();
            scopes.peek().forEach((name, information) -> names.put(information.id, name));
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
            useVariable(variable.line, variable.name);
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
            ResolvedStatement thenBranch = resolveStatement(anIf.thenBranch);
            ResolvedStatement elseBranch =
                    anIf.elseBranch == null ? null : resolveStatement(anIf.elseBranch);
            return new ResolvedStatement.If(anIf.line,
                    resolveExpression(anIf.condition),
                    thenBranch,
                    elseBranch
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
            int variableId = defineVariable(var.line, var.name);
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

            reportUnused();

            endScope();

            return new ResolvedStatement.Block(block.line,
                    resolvedStatements,
                    names, classes
            );
        }

        @Override
        public ResolvedStatement visitClassStatement(Statement.Class klass) {
            int classVariable = defineVariable(klass.line, klass.name, true);

            List<ResolvedStatement.Method> methods = resolveMethods(klass.methods);
            List<ResolvedStatement.Method> staticMethods = resolveMethods(klass.staticMethods);
            List<ResolvedStatement.Method> constructors = resolveMethods(klass.constructors);

            defineClass(classVariable, new ResolvedStatement.Class(klass.line,
                            staticMethods,
                            constructors,
                            methods
                    )
            );
            return null;
        }

        private void defineClass(int classVariable, ResolvedStatement.Class resolvedClass) {
            classes.peek().put(
                    classVariable,
                    resolvedClass
            );
        }

        private List<ResolvedStatement.Method> resolveMethods(List<Statement.Method> methods) {
            List<ResolvedStatement.Method> resolvedMethods = new ArrayList<>();
            for (Statement.Method method : methods) {
                beginScope();
                List<Integer> argumentsIds =
                        method.argumentsNames == null ? null : method.argumentsNames.stream()
                                .map(name -> defineVariable(method.line, name))
                                .collect(Collectors.toList());
                resolvedMethods.add(
                        new ResolvedStatement.Method(
                                method.line,
                                method.name,
                                argumentsIds,
                                visitBlockStatement(method.body),
                                definedVariables()
                        )
                );
                endScope();
            }
            return resolvedMethods;
        }

        @Override
        public ResolvedStatement visitReturnStatement(Statement.Return aReturn) {
            return new ResolvedStatement.Return(aReturn.line,
                    resolveExpression(aReturn.value)
            );
        }

        private static class VariableInformation {
            public int id;
            public int line;
            public boolean isClass;
            public boolean used;
            public boolean defined;

            public VariableInformation(int line, int id, boolean isClass) {
                this.line = line;
                this.id = id;
                this.isClass = isClass;
            }
        }
    }
}
