package com.chelseasinterpreter;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitBinaryExpr(Binary expr);

        R visitGroupingExpr(Grouping expr);

        R visitLiteralExpr(Literal expr);

        R visitUnaryExpr(Unary expr);

        R visitConditionalExpr(Conditional expr);
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final Expr left;
        final Token operator;
        final Expr right;

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("Expr.Binary\n");
            builder.append("left: " + this.left + "\n");
            builder.append("operator: " + this.operator + "\n");
            builder.append("right: " + this.right + "\n");
            return builder.toString();
        }
    }

    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        final Expr expression;

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("Expr.Grouping\n");
            builder.append("expression: " + this.expression + "\n");
            return builder.toString();
        }
    }

    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        final Object value;

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("Expr.Literal\n");
            builder.append("value: " + this.value + "\n");
            return builder.toString();
        }
    }

    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        final Token operator;
        final Expr right;

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("Expr.Unary\n");
            builder.append("operator: " + this.operator + "\n");
            builder.append("right: " + this.right + "\n");
            return builder.toString();
        }
    }

    static class Conditional extends Expr {
        Conditional(Expr condition, Expr thenBranch, Expr elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        final Expr condition;
        final Expr thenBranch;
        final Expr elseBranch;

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitConditionalExpr(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("Expr.Conditional\n");
            builder.append("condition: " + this.condition + "\n");
            builder.append("thenBranch: " + this.thenBranch + "\n");
            builder.append("elseBranch: " + this.elseBranch + "\n");
            return builder.toString();
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
}
