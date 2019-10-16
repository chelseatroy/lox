package com.chelseasinterpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chelseasinterpreter.TokenType.*;

class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int cursorIndex = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while ((!isAtEnd())) {
            start = cursorIndex;

            //Done in this order to prevent an infinite loop
            //if it's an invalid character.
            cursorIndex++;
            scanToken(source.charAt(cursorIndex - 1));
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return cursorIndex >= source.length();
    }

    private void scanToken(char character) {
        switch (character) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(consumeIfNext('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(consumeIfNext('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(consumeIfNext('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(consumeIfNext('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (consumeIfNext('/')) {
                    // A comment goes until the end of the line.
                    while (!isNextChar('\n') && !isAtEnd()) cursorIndex++;
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ': //God, I hate this about Java
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(character)) {
                    number();
                } else if (isAlpha(character)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void identifier() {
        while (isAlphaNumeric(source.charAt(cursorIndex))) cursorIndex++;

        String text = source.substring(start, cursorIndex);

        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);

    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void number() {
        while (isDigit(source.charAt(cursorIndex))) cursorIndex++;

        // Look for a fractional part.
        if (isNextChar('.') && isDigit(source.charAt(cursorIndex + 1))) {
            // Consume the "."
            cursorIndex++;

            while (isDigit(source.charAt(cursorIndex))) cursorIndex++;
        }

        addToken(NUMBER,
                Double.parseDouble(source.substring(start, cursorIndex)));
    }

    private void string() {
        while (!isNextChar('"') && !isAtEnd()) {
            if (isNextChar('\n')) line++;
            cursorIndex++;
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        cursorIndex++;

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, cursorIndex - 1);

        addToken(STRING, value);
    }

    private boolean consumeIfNext(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(cursorIndex) != expected) return false;

        cursorIndex++;
        return true;
    }

    private boolean isNextChar(char expected) {
        if (isAtEnd()) return false;
        return expected == source.charAt(cursorIndex);
    }


    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, cursorIndex);
        tokens.add(new Token(type, text, literal, line));
    }

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

}