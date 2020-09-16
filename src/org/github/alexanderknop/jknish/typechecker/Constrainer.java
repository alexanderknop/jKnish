package org.github.alexanderknop.jknish.typechecker;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class Constrainer {
    private final Set<Inequality> cash = new HashSet<>();

    void constrain(SimpleType left, SimpleType right, TypeErrorMessage message) {
        final Inequality inequality = new Inequality(left, right);
        if (cash.contains(inequality)) {
            return;
        } else {
            cash.add(inequality);
        }


        if (left instanceof SimpleType.Labeled && right instanceof SimpleType.Labeled) {
            SimpleType.Labeled labeledLeft = (SimpleType.Labeled) left;
            SimpleType.Labeled labeledRight = (SimpleType.Labeled) right;

            if (!labeledLeft.name.equals(labeledRight.name)) {
                message.send();
            }
        } else if (left instanceof SimpleType.Class && right instanceof SimpleType.Class) {
            SimpleType.Class leftClass = (SimpleType.Class) left;
            SimpleType.Class rightClass = (SimpleType.Class) right;

            for (var method : rightClass.methods.entrySet()) {
                if (!leftClass.methods.containsKey(method.getKey())) {
                    message.send();
                    break;
                }
                constrain(leftClass.methods.get(method.getKey()), method.getValue(), message);
            }
        } else if (left instanceof SimpleType.Variable) {
            SimpleType.Variable variable = (SimpleType.Variable) left;
            variable.upperBound.add(right);
            variable.lowerBound
                    .forEach(lowerBound -> constrain(lowerBound, right, message));
        } else if (right instanceof SimpleType.Variable) {
            SimpleType.Variable variable = (SimpleType.Variable) right;
            variable.lowerBound.add(left);
            variable.upperBound
                    .forEach(upperBound -> constrain(left, upperBound, message));
        } else if (left instanceof SimpleType.Labeled) {
            SimpleType.Labeled labeledLeft = (SimpleType.Labeled) left;
            constrain(labeledLeft.type, right, message);
        } else {
            message.send();
        }
    }

    private void constrain(SimpleType.Method left, SimpleType.Method right, TypeErrorMessage message) {
        constrain(left.value, right.value, message);
        if (left.arguments != null) {
            for (int i = 0; i < left.arguments.size(); i++) {
                constrain(right.arguments.get(i), left.arguments.get(i), message);
            }
        }
    }

    private static class Inequality {
        private final SimpleType left;
        private final SimpleType right;

        public Inequality(SimpleType left, SimpleType right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Inequality that = (Inequality) o;
            return Objects.equals(left, that.left) &&
                    Objects.equals(right, that.right);
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, right);
        }

        @Override
        public String toString() {
            return left + " <: " + right;
        }
    }
}
