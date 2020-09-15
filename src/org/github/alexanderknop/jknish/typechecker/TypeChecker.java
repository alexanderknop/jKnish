package org.github.alexanderknop.jknish.typechecker;

import org.github.alexanderknop.jknish.ErrorReporter;
import org.github.alexanderknop.jknish.objects.KnishCore;
import org.github.alexanderknop.jknish.objects.KnishModule;
import org.github.alexanderknop.jknish.objects.MethodId;
import org.github.alexanderknop.jknish.parser.Expression;
import org.github.alexanderknop.jknish.parser.Statement;

import java.util.*;
import java.util.stream.Collectors;

public class TypeChecker implements Statement.Visitor<Void>, Expression.Visitor<SimpleType> {
    public static void check(KnishCore core, List<Statement> statements, ErrorReporter reporter) {
        TypeChecker checker = new TypeChecker(core, statements, reporter);

        checker.check();
    }

    private final KnishCore core;
    private final Constrainer constrainer = new Constrainer();

    private SimpleType numberType;
    private SimpleType booleanType;
    private SimpleType stringType;

    private final List<Statement> statements;
    private final ErrorReporter reporter;

    private final Stack<HashMap<String, SimpleType>> scopes = new Stack<>();

    private TypeChecker(KnishCore core, List<Statement> statements, ErrorReporter reporter) {
        this.core = core;
        this.statements = statements;
        this.reporter = reporter;
    }

    private void check() {
        Map<KnishModule.Class, SimpleType> types = toSimpleType(core.getClasses());

        numberType = types.get(core.getClass("Number"));
        booleanType = types.get(core.getClass("Boolean"));
        stringType = types.get(core.getClass("String"));

        beginScope();

        for (var objectName : core.getObjects().keySet()) {
            scopes.peek().put(objectName, types.get(core.getObjectType(objectName)));
        }

        for (Statement statement : statements) {
            checkStatement(statement);
        }
        endScope();
    }

    private SimpleType expressionType(Expression expression) {
        return expression.accept(this);
    }

    private void checkStatement(Statement statement) {
        statement.accept(this);
    }

    private Map<KnishModule.Class, SimpleType> toSimpleType(Map<String, KnishModule.Class> classes) {
        Map<KnishModule.Class, SimpleType> types = new HashMap<>();
        Map<KnishModule.Class, SimpleType.Variable> classVariables = new HashMap<>();
        classes.forEach((className, klass) -> {
            final SimpleType.Variable variable = new SimpleType.Variable();
            classVariables.put(klass, variable);
            types.put(klass, new SimpleType.Labeled(className, variable));
        });

        classes.values().forEach(
                klass -> classToSimpleType(klass, classVariables.get(klass), types));
        return types;
    }

    private SimpleType classToSimpleType(KnishModule.Class klass,
                                         Map<KnishModule.Class, SimpleType> inProcess) {
        if (inProcess.containsKey(klass)) {
            return inProcess.get(klass);
        } else {
            SimpleType.Variable classVariable = new SimpleType.Variable();
            inProcess.put(klass, classVariable);

            return classToSimpleType(klass, classVariable, inProcess);
        }
    }

    private SimpleType.Variable classToSimpleType(KnishModule.Class klass,
                                                        SimpleType.Variable classVariable,
                                                        Map<KnishModule.Class, SimpleType> inProcess) {
        Map<MethodId, SimpleType.Method> methods = new HashMap<>();
        SimpleType.Class simpleClass = new SimpleType.Class(methods);
        klass.getMethods().forEach(
                (methodId, method) -> methods.put(methodId, methodToSimpleType(method, inProcess)));

        classVariable.lowerBound.add(simpleClass);
        classVariable.upperBound.add(simpleClass);
        return classVariable;
    }

    private SimpleType.Method methodToSimpleType(KnishModule.Method method,
                                                 Map<KnishModule.Class, SimpleType> inProcess) {
        List<SimpleType> arguments = null;
        if (method.getArguments() != null) {
            arguments = method.getArguments().stream().map(
                    argument -> intersectionToSimpleType(argument, inProcess)
            ).collect(Collectors.toList());
        }

        SimpleType value = unionToSimpleType(method.getValue(), inProcess);

        return new SimpleType.Method(arguments, value);
    }

    private SimpleType intersectionToSimpleType(KnishModule.Intersection intersection,
                                                Map<KnishModule.Class, SimpleType> inProcess) {
        SimpleType.Variable variable = new SimpleType.Variable();
        intersection.getTypes().stream()
                .map(klass -> classToSimpleType(klass, inProcess))
                .forEach(variable.upperBound::add);
        return variable;
    }

    private SimpleType unionToSimpleType(KnishModule.Union union,
                                         Map<KnishModule.Class, SimpleType> inProcess) {
        SimpleType.Variable variable = new SimpleType.Variable();
        union.getTypes().stream()
                .map(klass -> classToSimpleType(klass, inProcess))
                .forEach(variable.lowerBound::add);
        return variable;
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void endScope() {
        scopes.pop();
    }

    private SimpleType variableType(String variable) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(variable)) {
                return scopes.get(i).get(variable);
            }
        }

        throw new UnsupportedOperationException("The variable " + variable + " is not defined.");
    }

    private SimpleType.Variable define(String name) {
        SimpleType.Variable variable = new SimpleType.Variable();
        scopes.peek().put(name, variable);
        return variable;
    }


    @Override
    public SimpleType visitAssignExpression(Expression.Assign assign) {
        SimpleType valueType = expressionType(assign.value);
        SimpleType variableType = variableType(assign.variable);
        constrainer.constrain(valueType, variableType,
                new TypeErrorMessage(reporter, assign.line,
                        "Wrong type of the value assigned to " +
                                assign.variable + "."));
        return valueType;
    }

    @Override
    public SimpleType visitCallExpression(Expression.Call call) {
        List<SimpleType> arguments;
        MethodId methodId;
        if (call.arguments != null) {
            int arity = call.arguments.size();
            methodId = new MethodId(call.method, arity);
            arguments = new ArrayList<>();
            for (int i = 0; i < arity; i++) {
                arguments.add(new SimpleType.Variable());
            }
        } else {
            methodId = new MethodId(call.method, null);
            arguments = null;
        }

        SimpleType.Variable value = new SimpleType.Variable();
        SimpleType.Class klass = new SimpleType.Class(Map.of(methodId,
                new SimpleType.Method(arguments, value)));

        constrainer.constrain(expressionType(call.object), klass,
                new TypeErrorMessage(reporter, call.line,
                        "An object does not implement " + methodId + "."));

        if (call.arguments != null) {
            int arity = call.arguments.size();

            for (int i = 0; i < arity; i++) {
                constrainer.constrain(expressionType(call.arguments.get(i)), arguments.get(i),
                        new TypeErrorMessage(reporter, call.line,
                                "The value of " + i + "th argument has incompatible type."));
            }
        }

        return value;
    }

    @Override
    public SimpleType visitLiteralExpression(Expression.Literal literal) {
        if (literal.value == null) {
            return new SimpleType.Variable();
        } else if (literal.value instanceof Long) {
            return numberType;
        } else if (literal.value instanceof String) {
            return stringType;
        } else if (literal.value instanceof Boolean) {
            return booleanType;
        }

        throw new UnsupportedOperationException("Unknown type of the literal " + literal.value + ".");
    }

    @Override
    public SimpleType visitVariableExpression(Expression.Variable variable) {
        return variableType(variable.name);
    }

    @Override
    public SimpleType visitLogicalExpression(Expression.Logical logical) {
        constrainer.constrain(expressionType(logical.left), booleanType,
                new TypeErrorMessage(reporter, logical.line,
                        "Left operand of " + logical.operator + " must have type Boolean."));
        constrainer.constrain(expressionType(logical.right), booleanType,
                new TypeErrorMessage(reporter, logical.line,
                        "Left operand of " + logical.operator + " must have type Boolean."));

        return null;
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression expression) {
        expressionType(expression.expression);
        return null;
    }

    @Override
    public Void visitorIfStatement(Statement.If anIf) {
        constrainer.constrain(expressionType(anIf.condition), booleanType,
                new TypeErrorMessage(reporter, anIf.line,
                        "If conditions must have type Boolean."));
        checkStatement(anIf.thenBranch);
        checkStatement(anIf.elseBranch);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While aWhile) {
        constrainer.constrain(expressionType(aWhile.condition), booleanType,
                new TypeErrorMessage(reporter, aWhile.line,
                        "While conditions must have type Boolean."));
        checkStatement(aWhile.body);
        return null;
    }

    @Override
    public Void visitVarStatement(Statement.Var var) {
        SimpleType.Variable variable = define(var.name);
        if (var.initializer != null) {
            constrainer.constrain(expressionType(var.initializer), variable,
                    // this error is impossible since we create a fresh variable
                    new TypeErrorMessage(reporter, var.line, ""));
        }
        return null;
    }

    @Override
    public Void visitBlockStatement(Statement.Block block) {
        beginScope();
        for (Statement statement : block.statements) {
            checkStatement(statement);
        }
        endScope();
        return null;
    }
}
