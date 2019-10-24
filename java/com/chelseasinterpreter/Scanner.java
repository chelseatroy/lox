package com.chelseasinterpreter;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
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
        if (singleCharacterToken(character) != null) {
            addToken(singleCharacterToken(character));
            return;
        }

        if (isComparisonOperator(character)) {
            addComparisonOperatorTokenFor(character);
            return;
        }

        if (character == '/') {
            if (!isAComment()) {
                addToken(SLASH);
            }
            return;
        }

        switch (character) {
            case ' ': //God, I hate this about Java
            case '\r':
            case '\t':
                // Ignore whitespace.
                return;
            case '\n':
                line++;
                return;
        }

        if (character == '"') {
            addStringToken();
            return;
        }

        if (isDigit(character)) {
            addNumberToken();
            return;
        }

        if (isAlpha(character)) {
            addIdentifierToken();
            return;
        }

        Lox.error(line, "Unexpected character.");
    }

    private boolean isComparisonOperator(char character) {
        return comparisonOperators.contains(character);
    }

    private void addComparisonOperatorTokenFor(char character) {
        switch (character) {
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
        }
    }

    private boolean isAComment() {
        if (consumeIfNext('/')) {
            // A comment goes until the end of the line.
            while (!isNextChar('\n') && !isAtEnd()) cursorIndex++;
            return true;
        } else if (consumeIfNext('*')) {
            while (!(isNextChar('*') && source.charAt(cursorIndex) == '/') && !isAtEnd()) cursorIndex++;
            cursorIndex++;
            return true;
        } else {
            return false;
        }
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void addIdentifierToken() {
        while (isAlphaNumeric(source.charAt(cursorIndex - 1))  &&
                !isNextChar(';') &&
                !isAtEnd()) cursorIndex++;

        String text = source.substring(start, cursorIndex);

        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private TokenType singleCharacterToken(char character) {
        TokenType type = singleCharacterTokens.get(character);
        return type;
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void addNumberToken() {
        while (isDigit(source.charAt(cursorIndex - 1)) &&
                !isNextChar(';') &&
                !isNextChar(reserved) && //Fix later?
                !isAtEnd()) {
            cursorIndex++;
        }

        // Look for a fractional part.
        if (isNextChar('.') && isDigit(source.charAt(cursorIndex + 1))) {
            // Consume the "."
            cursorIndex++;

            while (isDigit(source.charAt(cursorIndex))) cursorIndex++;
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start, cursorIndex)));
    }

    private void addStringToken() {
        while (!isNextChar('"') && !isAtEnd()) {
            if (isNextChar('\n')) line++;
            cursorIndex++;
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated addStringToken.");
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

    private boolean isNextChar(Set<Character> expected) {
        if (isAtEnd()) return false;
        return expected.contains(source.charAt(cursorIndex));
    }


    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, cursorIndex);
        tokens.add(new Token(type, text, literal, line));
    }


    private static final Map<String, TokenType> keywords;
    private static final Map<Character, TokenType> singleCharacterTokens;
    private static final List<Character> comparisonOperators;
    private static final Set<Character> reserved;


    static {
        comparisonOperators = new ArrayList<Character>();
        comparisonOperators.add('!');
        comparisonOperators.add('=');
        comparisonOperators.add('>');
        comparisonOperators.add('<');
    }

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    static {
        singleCharacterTokens = new HashMap<>();
        singleCharacterTokens.put('(', LEFT_PAREN);
        singleCharacterTokens.put(')', RIGHT_PAREN);
        singleCharacterTokens.put('{', LEFT_BRACE);
        singleCharacterTokens.put('}', RIGHT_BRACE);
        singleCharacterTokens.put(',', COMMA);
        singleCharacterTokens.put('.', DOT);
        singleCharacterTokens.put('-', MINUS);
        singleCharacterTokens.put('+', PLUS);
        singleCharacterTokens.put(';', SEMICOLON);
        singleCharacterTokens.put('*', STAR);
    }

    static {
        reserved = new HashSet<Character>(singleCharacterTokens.keySet());
        reserved.addAll(comparisonOperators);
        reserved.remove(",");
    }
}
