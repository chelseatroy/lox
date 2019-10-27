package com.chelseasinterpreter;

import java.util.List;

import static com.chelseasinterpreter.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int cursorIndex = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (Throwable error) {
            return null;
        }
    }

    private Expr expression() {
        return block();
    }

    private Expr block() {
        Expr expr = conditional();

        while (consuming(COMMA)) {
            Token operator = previousToken();
            Expr right = conditional();
            expr = new Expr.Binary(expr, operator, right);
        }
        System.out.println(expr.toString());
        return expr;
    }

    private Expr conditional() {
        Expr expr = equality();

        if (consuming(QUESTION_MARK)) {
            Expr thenBranch = expression();
            consume(COLON, "Expect ':' after then branch of conditional expression.");
            Expr elseBranch = conditional();
            expr = new Expr.Conditional(expr, thenBranch, elseBranch);
        }
        System.out.println(expr.toString());
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (consuming(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previousToken();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        System.out.println(expr.toString());
        return expr;
    }

    private Expr comparison() {
        Expr expr = addition();

        while (consuming(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previousToken();
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        System.out.println(expr.toString());
        return expr;
    }

    private Expr addition() {
        Expr expr = multiplication();

        while (consuming(MINUS, PLUS)) {
            Token operator = previousToken();
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }

        System.out.println(expr.toString());
        return expr;
    }

    private Expr multiplication() {
        Expr expr = unary();

        while (consuming(STAR, SLASH)) {
            Token operator = previousToken();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        System.out.println(expr.toString());
        return expr;
    }

    private Expr unary() {
        if (consuming(BANG, MINUS)) {
            Token operator = previousToken();
            Expr right = primary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (consuming(FALSE)) return new Expr.Literal(false);
        if (consuming(TRUE)) return new Expr.Literal(true);

        if (consuming(NIL)) return new Expr.Literal(null);

        if (consuming(NUMBER, STRING)) {
            return new Expr.Literal(previousToken().literal);
        }

        if (consuming(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");

            return new Expr.Grouping(expr);
        }

        error(nextToken(), "Expect expression.");
        return null;

    }

    private Token consume(TokenType expectedType, String errorMessage) {
        if (nextTokenIsA(expectedType)) {
            advance();
            return previousToken();
        }

        error(nextToken(), errorMessage);
        return null;
    }


    private boolean consuming(TokenType... types) {
        for (TokenType type : types) {
            if (nextTokenIsA(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean nextTokenIsA(TokenType type) {
        if (isAtEnd()) return false;
        return nextToken().type == type;
    }

    private void advance() {
        if (!isAtEnd()) cursorIndex++;
    }

    private boolean isAtEnd() {
        return nextToken().type == EOF;
    }

    private Token nextToken() {
        return tokens.get(cursorIndex);
    }

    private Token previousToken() {
        return tokens.get(cursorIndex - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previousToken().type == SEMICOLON) return;

            switch (nextToken().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

}
