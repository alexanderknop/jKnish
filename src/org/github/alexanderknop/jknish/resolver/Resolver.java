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
        private final Stack<Map<Integer, Statement.Class>> classes = new Stack<>();
        private final Stack<Map<String, Integer>> classScopes = new Stack<>();
        private final Stack<ClassScopeType> classScopeTypes = new Stack<>();
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

        private int freshId() {
            return currentVariable++;
        }

        private int beginClassScope(ClassScopeType type) {
            this.classScopes.push(new HashMap<>());
            this.classScopeTypes.push(type);

            int thisId = freshId();
            this.classScopes.peek().put("this", thisId);
            return thisId;
        }

        private void endClassScope() {
            classScopes.pop();
            classScopeTypes.pop();
        }

        private int staticFieldId(int line, String variable) {
            if (classScopes.isEmpty()) {
                reporter.error(line,
                        "Cannot reference a field " +
                        variable +
                        "outside of a class definition.");
                return defineVariable(line, variable);
            }

            Map<String, Integer> staticClassScope;
            if (classScopeTypes.peek() == ClassScopeType.STATIC) {
                staticClassScope = classScopes.peek();
            } else {
                staticClassScope = classScopes.get(classScopes.size() - 2);
            }

            if (staticClassScope.containsKey(variable)) {
                return staticClassScope.get(variable);
            } else {
                int newId = freshId();
                staticClassScope.put(
                        variable,
                        newId
                );
                return newId;
            }
        }

        private int fieldId(int line, String variable) {
            if (classScopes.isEmpty()) {
                reporter.error(line,
                        "Cannot reference a field " +
                                variable +
                                "outside of a class definition.");
                return defineVariable(line, variable);
            } else if (classScopeTypes.peek() == ClassScopeType.STATIC) {
                reporter.error(line,
                        "Cannot use an instance field " + variable
                                + " in a static method.");
                return defineVariable(line, variable);
            }

            Map<String, Integer> classScope = classScopes.peek();
            if (classScope.containsKey(variable)) {
                return classScope.get(variable);
            } else {
                int newId = freshId();
                classScope.put(
                        variable,
                        newId
                );
                return newId;
            }
        }

        private Map<Integer, String> definedFields() {
            HashMap<Integer, String> names = new HashMap<>();
            classScopes.peek().forEach((name, id) -> names.put(id, name));
            return Collections.unmodifiableMap(names);
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

        private int declareVariable(int line, String name) {
            int newId = freshId();
            scopes.peek().put(name,
                    new VariableInformation(line, newId, false, false));
            return newId;
        }

        private void finishDefinition(int line, String name) {
            variableInformation(line, name).defined = true;
        }

        private void defineClass(int classVariable, Statement.Class klass) {
            classes.peek().put(classVariable, klass);
        }

        private int defineVariable(int line, String name) {
            return defineVariable(line, name, false);
        }

        private int defineVariable(int line, String name, boolean isClass) {
            int newId = freshId();
            scopes.peek().put(name,
                    new VariableInformation(line, newId, true, isClass));
            return newId;
        }

        private VariableInformation variableInformation(int line, String variable) {
            for (int i = scopes.size() - 1; i >= 0; i--) {
                if (scopes.get(i).containsKey(variable)) {
                    return scopes.get(i).get(variable);
                }
            }

            reporter.error(line, "Undeclared variable " + variable + ".");

            defineVariable(line, variable);
            useVariable(line, variable);
            return variableInformation(line, variable);
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
            final Map<Integer, ResolvedStatement.Class> classMap = new HashMap<>();
            classes.peek().forEach(
                    (id, klass) -> classMap.put(id, resolveClass(klass))
            );
            return classMap;
        }

        private ResolvedStatement.Class resolveClass(Statement.Class klass) {
            int staticThisId = beginClassScope(ClassScopeType.STATIC);
            List<ResolvedStatement.Method> staticMethods =
                    resolveMethods(klass.staticMethods);

            int thisId = beginClassScope(ClassScopeType.REGULAR);

            List<ResolvedStatement.Method> methods =
                    resolveMethods(klass.methods);
            List<ResolvedStatement.Method> constructors =
                    resolveMethods(klass.constructors);

            Map<Integer, String> fields = definedFields();
            endClassScope();

            Map<Integer, String> staticFields = definedFields();
            endClassScope();

            return new ResolvedStatement.Class(klass.line,
                    staticMethods,
                    constructors,
                    methods,
                    fields,
                    staticFields, thisId, staticThisId
            );
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
        public ResolvedExpression visitAssignFieldExpression(Expression.AssignField assign) {
            int fieldId = fieldId(assign.line, assign.variable);
            return new ResolvedExpression.Assign(assign.line,
                    fieldId,
                    resolveExpression(assign.value)
            );
        }

        @Override
        public ResolvedExpression visitAssignStaticFieldExpression(Expression.AssignStaticField assign) {
            int variableId = staticFieldId(assign.line, assign.variable);
            return new ResolvedExpression.Assign(assign.line,
                    variableId,
                    resolveExpression(assign.value));
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
            VariableInformation information = variableInformation(variable.line, variable.name);

            if (!information.defined) {
                reporter.error(variable.line, "The variable " + variable.name +
                        " cannot be used in its own initializer.");
            }

            return new ResolvedExpression.Variable(variable.line,
                    information.id);
        }

        @Override
        public ResolvedExpression visitFieldExpression(Expression.Field field) {
            return new ResolvedExpression.Variable(
                    field.line,
                    fieldId(field.line, field.name)
            );
        }

        @Override
        public ResolvedExpression visitStaticFieldExpression(Expression.StaticField staticField) {
            //todo
            return null;
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
            int variableId = declareVariable(var.line, var.name);
            ResolvedStatement.Expression resolvedStatement = null;
            if (var.initializer != null) {
                resolvedStatement =
                        new ResolvedStatement.Expression(var.line,
                                new ResolvedExpression.Assign(var.line,
                                        variableId,
                                        resolveExpression(var.initializer)
                                )
                        );
            }
            finishDefinition(var.line, var.name);
            return resolvedStatement;
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
                    names, classes, resolvedStatements
            );
        }

        @Override
        public ResolvedStatement visitClassStatement(Statement.Class klass) {
            defineClass(defineVariable(klass.line, klass.name, true), klass);
            return null;
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
            public final int id;
            public final int line;
            public boolean isClass;
            public boolean used;
            public boolean defined;

            public VariableInformation(int line, int id,
                                       boolean defined, boolean isClass) {
                this.line = line;
                this.id = id;
                this.defined = defined;
                this.isClass = isClass;
            }
        }

        private enum ClassScopeType {
            STATIC, REGULAR
        }
    }
}
