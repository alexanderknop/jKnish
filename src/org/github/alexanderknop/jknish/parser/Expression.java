package org.github.alexanderknop.jknish.parser;

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
    }

    class Assign extends Expression {
        public final String variable;
        public final Expression value;

        public Assign(int line, String variable, Expression value) {
            super(line);
            this.variable = variable;
            this.value = value;
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitAssignExpression(this);
        }
    }

    class Call extends Expression {
        public final String method;
        public final Expression object;

        public Call(int line, String method, Expression object) {
            super(line);
            this.method = method;
            this.object = object;
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitCallExpression(this);
        }
    }

    class Literal extends Expression {
        public final Object value;

        public Literal(int line, Object value) {
            super(line);
            this.value = value;
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    class Variable extends Expression {
        public final String name;

        public Variable(int line, String name) {
            super(line);
            this.name = name;
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitVariableExpression(this);
        }
    }

    abstract <N> N accept(Visitor<N> visitor);
}
