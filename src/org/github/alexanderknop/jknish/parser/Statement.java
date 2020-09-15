package org.github.alexanderknop.jknish.parser;

import java.util.List;
import java.util.Objects;

public abstract class Statement {
    public final int line;

    Statement(int line) {
        this.line = line;
    }

    public interface Visitor<N> {
        N visitExpressionStatement(Expression expression);

        N visitorIfStatement(If anIf);

        N visitWhileStatement(While aWhile);

        N visitVarStatement(Var var);

        N visitBlockStatement(Block block);

        N visitClassStatement(Class klass);
    }

    public static class Expression extends Statement {
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
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    public static class If extends Statement {
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
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitorIfStatement(this);
        }
    }

    public static class While extends Statement {
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
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

    public static class Var extends Statement {
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
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitVarStatement(this);
        }
    }

    public static class Block extends Statement {
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
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    public static class Class extends Statement {
        public final List<Method> methods;
        public final List<Method> constructors;
        public final List<Method> staticMethods;
        public final String name;

        public Class(int line, String name,
                     List<Method> methods,
                     List<Method> constructors, List<Method> staticMethods) {
            super(line);
            this.name = name;
            this.methods = methods;
            this.constructors = constructors;
            this.staticMethods = staticMethods;
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitClassStatement(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Class aClass = (Class) o;
            return Objects.equals(methods, aClass.methods) &&
                    Objects.equals(name, aClass.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(methods, name);
        }

        @Override
        public String toString() {
            return "Class{" +
                    "methods=" + methods +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static class Method {
        private final int line;
        public final String name;
        public final List<String> argumentsNames;
        public final List<Statement> body;

        public Method(int line,
                      String name, List<String> argumentsNames, List<Statement> body) {
            this.line = line;
            this.name = name;
            this.argumentsNames = argumentsNames;
            this.body = body;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Method method = (Method) o;
            return line == method.line &&
                    Objects.equals(name, method.name) &&
                    Objects.equals(argumentsNames, method.argumentsNames) &&
                    Objects.equals(body, method.body);
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, name, argumentsNames, body);
        }

        @Override
        public String toString() {
            return "Method{" +
                    "line=" + line +
                    ", name='" + name + '\'' +
                    ", argumentsNames=" + argumentsNames +
                    ", body=" + body +
                    '}';
        }
    }

    public abstract <N> N accept(Visitor<N> visitor);
}
