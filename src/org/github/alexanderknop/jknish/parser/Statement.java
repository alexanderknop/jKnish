package org.github.alexanderknop.jknish.parser;

import java.util.Arrays;
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

        N visitReturnStatement(Return aReturn);
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
                    "line=" + line +
                    ", expression=" + expression +
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
                    "line=" + line +
                    ", condition=" + condition +
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
                    "line=" + line +
                    ", condition=" + condition +
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
        public Var(int line, String name) {
            this(line, name, null);
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
                    "line=" + line +
                    ", name='" + name + '\'' +
                    ", initializer=" + initializer +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitVarStatement(this);
        }
    }

    public static class Return extends Statement {
        public final org.github.alexanderknop.jknish.parser.Expression value;

        public Return(int line, org.github.alexanderknop.jknish.parser.Expression value) {
            super(line);
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Return aReturn = (Return) o;
            return Objects.equals(value, aReturn.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Return{" +
                    "line=" + line +
                    ", value=" + value +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitReturnStatement(this);
        }
    }

    public static class Block extends Statement {
        public final List<Statement> statements;

        public Block(int line, List<Statement> statements) {
            super(line);
            this.statements = statements;
        }

        public Block(int line, Statement... statements) {
            this(line, Arrays.asList(statements));
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
                     List<Method> staticMethods, List<Method> constructors, List<Method> methods) {
            super(line);
            this.name = name;
            this.methods = methods;
            this.constructors = constructors;
            this.staticMethods = staticMethods;
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
                    "line=" + line +
                    ", methods=" + methods +
                    ", constructors=" + constructors +
                    ", staticMethods=" + staticMethods +
                    ", name='" + name + '\'' +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitClassStatement(this);
        }
    }

    public final static class Method {
        public final String name;
        public final MethodBody body;

        public Method(int line,
                      String name, List<String> argumentsNames, List<Statement> body) {
            this.name = name;
            this.body = new MethodBody(
                    line, argumentsNames, new Block(line, body)
            );
        }

        public Method(int line,
                      String name, List<String> argumentsNames, Statement... body) {
            this(line, name, argumentsNames, Arrays.asList(body));
        }

        public Method(int line,
                      String name, Statement... body) {
            this(line, name, null, Arrays.asList(body));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Method method = (Method) o;
            return Objects.equals(name, method.name) &&
                    Objects.equals(body, method.body);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, body);
        }

        @Override
        public String toString() {
            return "Method{" +
                    "name='" + name + '\'' +
                    ", body=" + body +
                    '}';
        }
    }

    public final static class MethodBody {
        public final int line;
        public final List<String> argumentsNames;
        public final Statement.Block block;

        public MethodBody(int line, List<String> argumentsNames, Block block) {
            this.line = line;
            this.argumentsNames = argumentsNames;
            this.block = block;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodBody that = (MethodBody) o;
            return line == that.line &&
                    Objects.equals(argumentsNames, that.argumentsNames) &&
                    Objects.equals(block, that.block);
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, argumentsNames, block);
        }

        @Override
        public String toString() {
            return "MethodBody{" +
                    "line=" + line +
                    ", argumentsNames=" + argumentsNames +
                    ", block=" + block +
                    '}';
        }
    }

    public abstract <N> N accept(Visitor<N> visitor);
}
