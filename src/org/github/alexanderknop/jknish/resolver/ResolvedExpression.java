package org.github.alexanderknop.jknish.resolver;

import org.github.alexanderknop.jknish.parser.LogicalOperator;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class ResolvedExpression {
    public final int line;

    ResolvedExpression(int line) {
        this.line = line;
    }

    public interface Visitor<V> {
        V visitAssignExpression(Assign assign);

        V visitCallExpression(Call call);

        V visitLiteralExpression(Literal literal);

        V visitVariableExpression(Variable variable);

        V visitLogicalExpression(Logical logical);
    }

    public static class Assign extends ResolvedExpression {
        public final int variableId;
        public final ResolvedExpression value;

        public Assign(int line, int variableId, ResolvedExpression value) {
            super(line);
            this.variableId = variableId;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Assign assign = (Assign) o;
            return variableId == assign.variableId &&
                    Objects.equals(value, assign.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variableId, value);
        }

        @Override
        public String toString() {
            return "Assign{" +
                    "line=" + line +
                    ", variableId=" + variableId +
                    ", value=" + value +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitAssignExpression(this);
        }
    }

    public static class Call extends ResolvedExpression {
        public final ResolvedExpression object;
        public final String method;
        public final List<ResolvedExpression> arguments;

        public Call(int line, ResolvedExpression object, String method, ResolvedExpression... arguments) {
            this(line, object, method, Arrays.asList(arguments));
        }

        public Call(int line, ResolvedExpression object, String method) {
            super(line);
            this.object = object;
            this.method = method;
            this.arguments = null;
        }

        public Call(int line, ResolvedExpression object, String method, List<ResolvedExpression> arguments) {
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
                    "line=" + line +
                    ", object=" + object +
                    ", method='" + method + '\'' +
                    ", arguments=" + arguments +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitCallExpression(this);
        }
    }

    public static class Literal extends ResolvedExpression {
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
                    "line=" + line +
                    ", value=" + value +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    public static class Variable extends ResolvedExpression {
        public final int variableId;

        public Variable(int line, int variableId) {
            super(line);
            this.variableId = variableId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Variable variable = (Variable) o;
            return Objects.equals(variableId, variable.variableId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variableId);
        }

        @Override
        public String toString() {
            return "Variable{" +
                    "line=" + line +
                    ", variableId=" + variableId +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitVariableExpression(this);
        }

    }

    public static class Logical extends ResolvedExpression {
        public final ResolvedExpression left;
        public final LogicalOperator operator;
        public final ResolvedExpression right;

        public Logical(int line, ResolvedExpression left, LogicalOperator operator, ResolvedExpression right) {
            super(line);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Logical logical = (Logical) o;
            return Objects.equals(left, logical.left) &&
                    operator == logical.operator &&
                    Objects.equals(right, logical.right);
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, operator, right);
        }

        @Override
        public String toString() {
            return "Logical{" +
                    "line=" + line +
                    ", left=" + left +
                    ", operator=" + operator +
                    ", right=" + right +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitLogicalExpression(this);
        }
    }

    public abstract <N> N accept(Visitor<N> visitor);
}
