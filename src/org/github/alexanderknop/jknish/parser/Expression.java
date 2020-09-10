package org.github.alexanderknop.jknish.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class Expression {
    public final int line;

    Expression(int line) {
        this.line = line;
    }

    public interface Visitor<V> {
        V visitAssignExpression(Assign assign);

        V visitCallExpression(Call call);

        V visitLiteralExpression(Literal literal);

        V visitVariableExpression(Variable variable);

        V visitLogicalExpression(Logical logical);
    }

    static class Assign extends Expression {
        public final String variable;
        public final Expression value;

        public Assign(int line, String variable, Expression value) {
            super(line);
            this.variable = variable;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Assign assign = (Assign) o;
            return Objects.equals(variable, assign.variable) &&
                    Objects.equals(value, assign.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variable, value);
        }

        @Override
        public String toString() {
            return "Assign{" +
                    "variable='" + variable + '\'' +
                    ", value=" + value +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitAssignExpression(this);
        }
    }

    static class Call extends Expression {
        private final Expression object;
        public final String method;
        public final List<Expression> arguments;

        public Call(int line, Expression object, String method, Expression... arguments) {
            this(line, object, method, Arrays.asList(arguments));
        }

        public Call(int line, Expression object, String method) {
            super(line);
            this.object = object;
            this.method = method;
            this.arguments = null;
        }

        public Call(int line, Expression object, String method, List<Expression> arguments) {
            super(line);
            this.object = object;
            this.method = method;
            this.arguments = arguments;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Call call = (Call) o;
            return Objects.equals(object, call.object) &&
                    Objects.equals(method, call.method) &&
                    Objects.equals(arguments, call.arguments);
        }

        @Override
        public int hashCode() {
            return Objects.hash(object, method, arguments);
        }

        @Override
        public String toString() {
            return "Call{" +
                    "object=" + object +
                    ", method='" + method + '\'' +
                    ", arguments=" + arguments +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitCallExpression(this);
        }
    }

    static class Literal extends Expression {
        public final Object value;

        public Literal(int line, Object value) {
            super(line);
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Literal literal = (Literal) o;
            return Objects.equals(value, literal.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Literal{" +
                    "value=" + value +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    static class Variable extends Expression {
        public final String name;

        public Variable(int line, String name) {
            super(line);
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Variable variable = (Variable) o;
            return Objects.equals(name, variable.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "Variable{" +
                    "name='" + name + '\'' +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitVariableExpression(this);
        }

    }

    static class Logical extends Expression {
        public final Expression expression;
        public final LogicalOperator operator;
        public final Expression right;

        Logical(int line, Expression expression, LogicalOperator operator, Expression right) {
            super(line);
            this.expression = expression;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Logical logical = (Logical) o;
            return Objects.equals(expression, logical.expression) &&
                    operator == logical.operator &&
                    Objects.equals(right, logical.right);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expression, operator, right);
        }

        @Override
        public String toString() {
            return "Logical{" +
                    "expression=" + expression +
                    ", operator=" + operator +
                    ", right=" + right +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitLogicalExpression(this);
        }
    }

    abstract <N> N accept(Visitor<N> visitor);
}
