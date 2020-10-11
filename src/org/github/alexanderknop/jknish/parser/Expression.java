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

        V visitAssignFieldExpression(AssignField assign);

        V visitAssignStaticFieldExpression(AssignStaticField assign);

        V visitCallExpression(Call call);

        V visitLiteralExpression(Literal literal);

        V visitVariableExpression(Variable variable);

        V visitLogicalExpression(Logical logical);

        V visitFieldExpression(Field field);

        V visitStaticFieldExpression(StaticField staticField);

        V visitThisExpression(This aThis);
    }

    public static class Assign extends Expression {
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
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitAssignExpression(this);
        }
    }

    public static class AssignField extends Expression {
        public final String variable;
        public final Expression value;

        public AssignField(int line, String variable, Expression value) {
            super(line);
            this.variable = variable;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AssignField assign = (AssignField) o;
            return Objects.equals(variable, assign.variable) &&
                    Objects.equals(value, assign.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variable, value);
        }

        @Override
        public String toString() {
            return "AssignField{" +
                    "variable='" + variable + '\'' +
                    ", value=" + value +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitAssignFieldExpression(this);
        }
    }

    public static class AssignStaticField extends Expression {
        public final String variable;
        public final Expression value;

        public AssignStaticField(int line, String variable, Expression value) {
            super(line);
            this.variable = variable;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AssignStaticField assign = (AssignStaticField) o;
            return Objects.equals(variable, assign.variable) &&
                    Objects.equals(value, assign.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(variable, value);
        }

        @Override
        public String toString() {
            return "AssignStaticField{" +
                    "variable='" + variable + '\'' +
                    ", value=" + value +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitAssignStaticFieldExpression(this);
        }
    }

    public static class Call extends Expression {
        public final Expression object;
        public final String method;
        public final Statement.MethodBody block;
        public final List<Expression> arguments;

        public Call(int line, Expression object, String method,
                    Statement.MethodBody block, Expression... arguments) {
            this(line, object, method, block, Arrays.asList(arguments));
        }

        public Call(int line, Expression object, String method) {
            this(line, object, method, null, (List<Expression>) null);
        }

        public Call(int line, Expression object, String method, Expression... arguments) {
            this(line, object, method, null, Arrays.asList(arguments));
        }

        public Call(int line, Expression object, String method,
                    Statement.MethodBody block) {
            this(line, object, method, block, (List<Expression>) null);
        }

        public Call(int line, Expression object, String method,
                    List<Expression> arguments) {
            this(line, object, method, null, arguments);
        }


        public Call(int line, Expression object, String method,
                    Statement.MethodBody block, List<Expression> arguments) {
            super(line);
            this.object = object;
            this.method = method;
            this.block = block;
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
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitCallExpression(this);
        }
    }

    public static class Literal extends Expression {
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
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    public static class Variable extends Expression {
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
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitVariableExpression(this);
        }

    }

    public static class Field extends Expression {
        public final String name;

        public Field(int line, String name) {
            super(line);
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Field field = (Field) o;
            return Objects.equals(name, field.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "Field{" +
                    "line=" + line +
                    ", name='" + name + '\'' +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitFieldExpression(this);
        }
    }

    public static class StaticField extends Expression {
        public final String name;

        public StaticField(int line, String name) {
            super(line);
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StaticField that = (StaticField) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "StaticField{" +
                    "line=" + line +
                    ", name='" + name + '\'' +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitStaticFieldExpression(this);
        }
    }

    public static class Logical extends Expression {
        public final Expression left;
        public final LogicalOperator operator;
        public final Expression right;

        public Logical(int line, Expression left, LogicalOperator operator, Expression right) {
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
                    "expression=" + left +
                    ", operator=" + operator +
                    ", right=" + right +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitLogicalExpression(this);
        }
    }

    public static class This extends Expression {
        public This(int line) {
            super(line);
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitThisExpression(this);
        }
    }

    public abstract <N> N accept(Visitor<N> visitor);
}
