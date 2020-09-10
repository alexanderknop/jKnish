package org.github.alexanderknop.jknish.parser;

import java.util.List;
import java.util.Objects;

public abstract class Statement {
    public final int line;

    Statement(int line) {
        this.line = line;
    }

    interface Visitor<N> {
        N visitExpressionStatement(Expression expression);

        N visitorIfStatement(If anIf);

        N visitWhileStatement(While aWhile);

        N visitVarStatement(Var var);

        N visitReturnStatement(Return aReturn);

        N visitBlockStatement(Block block);
    }

    static class Expression extends Statement {
        public final org.github.alexanderknop.jknish.parser.Expression expression;

        public Expression(int line, org.github.alexanderknop.jknish.parser.Expression expression) {
            super(line);
            this.expression = expression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Expression that = (Expression) o;
            return Objects.equals(expression, that.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expression);
        }

        @Override
        public String toString() {
            return "Expression{" +
                    "expression=" + expression +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    static class If extends Statement {
        public final org.github.alexanderknop.jknish.parser.Expression condition;
        public final Statement thenBranch;
        public final Statement elseBranch;

        public If(int line, org.github.alexanderknop.jknish.parser.Expression condition,
                  Statement thenBranch, Statement elseBranch) {
            super(line);
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            If anIf = (If) o;
            return Objects.equals(condition, anIf.condition) &&
                    Objects.equals(thenBranch, anIf.thenBranch) &&
                    Objects.equals(elseBranch, anIf.elseBranch);
        }

        @Override
        public int hashCode() {
            return Objects.hash(condition, thenBranch, elseBranch);
        }

        @Override
        public String toString() {
            return "If{" +
                    "condition=" + condition +
                    ", thenBranch=" + thenBranch +
                    ", elseBranch=" + elseBranch +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitorIfStatement(this);
        }
    }

    static class While extends Statement {
        public final org.github.alexanderknop.jknish.parser.Expression condition;
        public final Statement body;

        public While(int line, org.github.alexanderknop.jknish.parser.Expression condition,
                     Statement body) {
            super(line);
            this.condition = condition;
            this.body = body;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            While aWhile = (While) o;
            return Objects.equals(condition, aWhile.condition) &&
                    Objects.equals(body, aWhile.body);
        }

        @Override
        public int hashCode() {
            return Objects.hash(condition, body);
        }

        @Override
        public String toString() {
            return "While{" +
                    "condition=" + condition +
                    ", body=" + body +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

    static class Var extends Statement {
        public final String name;
        public final org.github.alexanderknop.jknish.parser.Expression initializer;

        public Var(int line, String name,
                   org.github.alexanderknop.jknish.parser.Expression initializer) {
            super(line);
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Var var = (Var) o;
            return Objects.equals(name, var.name) &&
                    Objects.equals(initializer, var.initializer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, initializer);
        }

        @Override
        public String toString() {
            return "Var{" +
                    "name='" + name + '\'' +
                    ", initializer=" + initializer +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitVarStatement(this);
        }
    }

    static class Return extends Statement {
        public final org.github.alexanderknop.jknish.parser.Expression expression;

        public Return(int line, org.github.alexanderknop.jknish.parser.Expression expression) {
            super(line);
            this.expression = expression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Return aReturn = (Return) o;
            return Objects.equals(expression, aReturn.expression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expression);
        }

        @Override
        public String toString() {
            return "Return{" +
                    "expression=" + expression +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitReturnStatement(this);
        }
    }

    static class Block extends Statement {
        public final List<Statement> statements;

        public Block(int line, List<Statement> statements) {
            super(line);
            this.statements = statements;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Block block = (Block) o;
            return Objects.equals(statements, block.statements);
        }

        @Override
        public int hashCode() {
            return Objects.hash(statements);
        }

        @Override
        public String toString() {
            return "Block{" +
                    "statements=" + statements +
                    '}';
        }

        @Override
        <N> N accept(Visitor<N> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    abstract <N> N accept(Visitor<N> visitor);
}
