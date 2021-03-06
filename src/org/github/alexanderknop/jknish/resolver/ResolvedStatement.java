package org.github.alexanderknop.jknish.resolver;

import org.github.alexanderknop.jknish.parser.MethodId;

import java.util.*;

import static java.util.Collections.*;

public abstract class ResolvedStatement {
    public final int line;

    ResolvedStatement(int line) {
        this.line = line;
    }

    public interface Visitor<N> {
        N visitExpressionStatement(Expression expression);

        N visitorIfStatement(If anIf);

        N visitWhileStatement(While aWhile);

        N visitBlockStatement(Block block);

        N visitReturnStatement(Return aReturn);
    }

    public static class Expression extends ResolvedStatement {
        public final ResolvedExpression resolvedExpression;

        public Expression(int line, ResolvedExpression resolvedExpression) {
            super(line);
            this.resolvedExpression = resolvedExpression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Expression that = (Expression) o;
            return Objects.equals(resolvedExpression, that.resolvedExpression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resolvedExpression);
        }

        @Override
        public String toString() {
            return "Expression{" +
                    "line=" + line +
                    ", expression=" + resolvedExpression +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    public static class If extends ResolvedStatement {
        public final ResolvedExpression condition;
        public final ResolvedStatement thenBranch;
        public final ResolvedStatement elseBranch;

        public If(int line, ResolvedExpression condition,
                  ResolvedStatement thenBranch, ResolvedStatement elseBranch) {
            super(line);
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        public If(int line, ResolvedExpression condition,
                  ResolvedStatement thenBranch) {
            this(line, condition, thenBranch, null);
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

    public static class While extends ResolvedStatement {
        public final ResolvedExpression condition;
        public final ResolvedStatement body;

        public While(int line, ResolvedExpression condition,
                     ResolvedStatement body) {
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

    public static class Return extends ResolvedStatement {
        public final ResolvedExpression value;

        public Return(int line, ResolvedExpression value) {
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

    public static class Block extends ResolvedStatement {
        public final List<ResolvedStatement> resolvedStatements;
        public final Map<Integer, String> names;
        public final Map<Integer, Class> classes;

        public Block(int line, Map<Integer, String> names, Map<Integer, Class> classes,
                     List<ResolvedStatement> resolvedStatements) {
            super(line);
            this.resolvedStatements = resolvedStatements;
            this.names = names;
            this.classes = classes;
        }

        public Block(int line, Map<Integer, String> names, Map<Integer, Class> classes,
                     ResolvedStatement... resolvedStatements) {
            this(line, names, classes, Arrays.asList(resolvedStatements));
        }

        public Block(int line, Map<Integer, String> names,
                     ResolvedStatement... resolvedStatements) {
            this(line, names, emptyMap(), Arrays.asList(resolvedStatements));
        }

        public Block(int line,
                     ResolvedStatement... resolvedStatements) {
            this(line, emptyMap(), emptyMap(), Arrays.asList(resolvedStatements));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Block block = (Block) o;
            return Objects.equals(resolvedStatements, block.resolvedStatements) &&
                    Objects.equals(names, block.names) &&
                    Objects.equals(classes, block.classes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(resolvedStatements, names, classes);
        }

        @Override
        public String toString() {
            return "Block{" +
                    "line=" + line +
                    ", resolvedStatements=" + resolvedStatements +
                    ", names=" + names +
                    ", classes=" + classes +
                    '}';
        }

        @Override
        public <N> N accept(Visitor<N> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    public static class Class {
        public final int line;
        public final Map<MethodId, Method> methods;
        public final Map<MethodId, Method> constructors;
        public final Map<MethodId, Method> staticMethods;
        public final Map<Integer, String> fields;
        public final Map<Integer, String> staticFields;
        public final int thisId;
        public final int staticThisId;

        public Class(int line,
                     Map<MethodId, Method> staticMethods,
                     Map<MethodId, Method> constructors,
                     Map<MethodId, Method> methods,
                     Map<Integer, String> fields, Map<Integer, String> staticFields,
                     int thisId, int staticThisId) {
            this.line = line;
            this.methods = methods;
            this.constructors = constructors;
            this.staticMethods = staticMethods;
            this.fields = fields;
            this.staticFields = staticFields;
            this.thisId = thisId;
            this.staticThisId = staticThisId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Class aClass = (Class) o;
            return line == aClass.line &&
                    thisId == aClass.thisId &&
                    staticThisId == aClass.staticThisId &&
                    Objects.equals(methods, aClass.methods) &&
                    Objects.equals(constructors, aClass.constructors) &&
                    Objects.equals(staticMethods, aClass.staticMethods) &&
                    Objects.equals(fields, aClass.fields) &&
                    Objects.equals(staticFields, aClass.staticFields);
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, methods, constructors, staticMethods, fields, staticFields, thisId, staticThisId);
        }

        @Override
        public String toString() {
            return "Class{" +
                    "line=" + line +
                    ", methods=" + methods +
                    ", constructors=" + constructors +
                    ", staticMethods=" + staticMethods +
                    ", fields=" + fields +
                    ", staticFields=" + staticFields +
                    ", thisId=" + thisId +
                    ", staticThisId=" + staticThisId +
                    '}';
        }
    }

    public final static class Method {
        public final int line;
        public final List<Integer> argumentsIds;
        public final Map<Integer, String> argumentNames;
        public final Block body;

        public Method(int line,
                      List<Integer> argumentsIds, Block body,
                      Map<Integer, String> argumentNames) {
            this.line = line;
            this.argumentsIds = argumentsIds;
            this.argumentNames = argumentNames;
            this.body = body;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Method method = (Method) o;
            return line == method.line &&
                    Objects.equals(argumentsIds, method.argumentsIds) &&
                    Objects.equals(body, method.body);
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, argumentsIds, body);
        }

        @Override
        public String toString() {
            return "Method{" +
                    "line=" + line +
                    ", argumentsNames=" + argumentsIds +
                    ", body=" + body +
                    '}';
        }
    }

    public abstract <N> N accept(Visitor<N> visitor);
}
