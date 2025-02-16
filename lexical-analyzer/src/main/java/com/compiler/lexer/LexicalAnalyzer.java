package com.compiler.lexer;

import com.compiler.error.ErrorHandler;
import java.util.*;

public class LexicalAnalyzer {
    private final SourceReader reader;
    private final ErrorHandler errorHandler;
    private final List<Token> tokens;
    private final Map<String, TokenType> keywords;
    private int tokenCount = 0;

    public LexicalAnalyzer(String input, ErrorHandler errorHandler) {
        this.reader = new SourceReader(input);
        this.errorHandler = errorHandler;
        this.tokens = new ArrayList<>();
        this.keywords = initializeKeywords();
    }

    private Map<String, TokenType> initializeKeywords() {
        Map<String, TokenType> keywords = new HashMap<>();
        keywords.put("int", TokenType.INT);
        keywords.put("dec", TokenType.DEC);
        keywords.put("bool", TokenType.BOOL);
        keywords.put("char", TokenType.CHAR);
        keywords.put("str", TokenType.STR);
        keywords.put("in", TokenType.IN);
        keywords.put("out", TokenType.OUT);
        keywords.put("outln", TokenType.OUTLN);
        keywords.put("true", TokenType.BOOLEAN_LITERAL);
        keywords.put("false", TokenType.BOOLEAN_LITERAL);
        return keywords;
    }

    public List<Token> tokenize() {
        while (reader.hasNext()) {
            skipWhitespace();
            
            if (!reader.hasNext()) {
                break;
            }

            Token token = nextToken();
            if (token != null) {
                tokens.add(token);
                tokenCount++;
            }
        }
        
        tokens.add(new Token(TokenType.EOF, "", reader.getLine(), reader.getColumn()));
        return tokens;
    }

    private void skipWhitespace() {
        while (reader.hasNext() && Character.isWhitespace(reader.current())) {
            reader.advance();
        }
    }

    private Token nextToken() {
        char current = reader.current();
        int line = reader.getLine();
        int column = reader.getColumn();

        // Handle global variable marker
        if (current == '@') {
            reader.advance();
            return new Token(TokenType.GLOBAL, "@", line, column);
        }

        // Handle comments
        if (current == '#') {
            reader.advance();
            if (reader.hasNext() && reader.current() == '*') {
                reader.advance();
                return handleMultiLineComment(line, column);
            }
            return handleSingleLineComment(line, column);
        }

        // Handle identifiers and keywords
        if (isLetter(current)) {
            return handleIdentifierOrKeyword(line, column);
        }

        // Handle numbers
        if (isDigit(current)) {
            return handleNumber(line, column);
        }

        // Handle strings
        if (current == '"') {
            return handleString(line, column);
        }

        // Handle characters
        if (current == '\'') {
            return handleCharacter(line, column);
        }

        // Handle comparison operators
        if (current == '=' || current == '!' || current == '<' || current == '>') {
            return handleComparisonOperator(line, column);
        }

        // Handle other operators and symbols
        return handleOperator(line, column);
    }

    private Token handleIdentifierOrKeyword(int line, int column) {
        StringBuilder builder = new StringBuilder();

        while (reader.hasNext() && (isLetter(reader.current()) || isDigit(reader.current()))) {
            if (builder.length() >= 32) {
                errorHandler.addError(line, column, "Identifier too long (max 32 characters)");
                return new Token(TokenType.INVALID, builder.toString(), line, column);
            }
            builder.append(reader.current());
            reader.advance();
        }

        String word = builder.toString();
        
        // Check if identifier is all lowercase
        if (!word.equals(word.toLowerCase())) {
            errorHandler.addError(line, column, "Identifiers must be lowercase");
            return new Token(TokenType.INVALID, word, line, column);
        }

        return new Token(keywords.getOrDefault(word, TokenType.IDENTIFIER), word, line, column);
    }

    private Token handleNumber(int line, int column) {
        StringBuilder builder = new StringBuilder();
        boolean isDecimal = false;
        int decimalPlaces = 0;

        while (reader.hasNext() && (isDigit(reader.current()) || reader.current() == '.')) {
            if (reader.current() == '.') {
                if (isDecimal) {
                    errorHandler.addError(line, column, "Invalid number format: multiple decimal points");
                    return new Token(TokenType.INVALID, builder.toString(), line, column);
                }
                isDecimal = true;
            } else if (isDecimal) {
                decimalPlaces++;
                if (decimalPlaces > 5) {
                    errorHandler.addError(line, column, "Decimal numbers cannot exceed 5 decimal places");
                    return new Token(TokenType.INVALID, builder.toString(), line, column);
                }
            }
            builder.append(reader.current());
            reader.advance();
        }

        return new Token(isDecimal ? TokenType.DECIMAL_LITERAL : TokenType.INTEGER_LITERAL, 
                        builder.toString(), line, column);
    }

    private Token handleString(int line, int column) {
        StringBuilder builder = new StringBuilder();
        reader.advance(); // Skip opening quote

        while (reader.hasNext() && reader.current() != '"') {
            builder.append(reader.current());
            reader.advance();
        }

        if (!reader.hasNext()) {
            errorHandler.addError(line, column, "Unterminated string literal");
            return new Token(TokenType.INVALID, builder.toString(), line, column);
        }

        reader.advance(); // Skip closing quote
        return new Token(TokenType.STRING_LITERAL, builder.toString(), line, column);
    }

    private Token handleCharacter(int line, int column) {
        reader.advance(); // Skip opening quote

        if (!reader.hasNext()) {
            errorHandler.addError(line, column, "Unterminated character literal");
            return new Token(TokenType.INVALID, "", line, column);
        }

        char value = reader.current();
        reader.advance();

        if (!reader.hasNext() || reader.current() != '\'') {
            errorHandler.addError(line, column, "Unterminated character literal");
            return new Token(TokenType.INVALID, String.valueOf(value), line, column);
        }

        reader.advance(); // Skip closing quote
        return new Token(TokenType.CHARACTER_LITERAL, String.valueOf(value), line, column);
    }

    private Token handleComparisonOperator(int line, int column) {
        char current = reader.current();
        reader.advance();

        if (reader.hasNext() && reader.current() == '=') {
            reader.advance();
            if (current == '=') return new Token(TokenType.EQUALS, "==", line, column);
            if (current == '!') return new Token(TokenType.NOT_EQUALS, "!=", line, column);
        }

        if (current == '<') return new Token(TokenType.LESS_THAN, "<", line, column);
        if (current == '>') return new Token(TokenType.GREATER_THAN, ">", line, column);

        return new Token(current == '=' ? TokenType.ASSIGN : TokenType.INVALID, 
                        String.valueOf(current), line, column);
    }

    private Token handleOperator(int line, int column) {
        char current = reader.current();
        reader.advance();

        switch (current) {
            case '+': return new Token(TokenType.PLUS, "+", line, column);
            case '-': return new Token(TokenType.MINUS, "-", line, column);
            case '*': return new Token(TokenType.MULTIPLY, "*", line, column);
            case '/': return new Token(TokenType.DIVIDE, "/", line, column);
            case '%': return new Token(TokenType.MODULUS, "%", line, column);
            case '^': return new Token(TokenType.EXPONENT, "^", line, column);
            case ';': return new Token(TokenType.SEMICOLON, ";", line, column);
            default:
                errorHandler.addError(line, column, "Invalid character: " + current);
                return new Token(TokenType.INVALID, String.valueOf(current), line, column);
        }
    }

    private Token handleSingleLineComment(int line, int column) {
        StringBuilder comment = new StringBuilder();

        while (reader.hasNext() && reader.current() != '\n') {
            comment.append(reader.current());
            reader.advance();
        }

        return new Token(TokenType.SINGLE_COMMENT, comment.toString(), line, column);
    }

    private Token handleMultiLineComment(int line, int column) {
        StringBuilder comment = new StringBuilder();

        while (reader.hasNext()) {
            if (reader.current() == '*') {
                reader.advance();
                if (reader.hasNext() && reader.current() == '#') {
                    reader.advance();
                    return new Token(TokenType.MULTI_COMMENT_START, comment.toString(), line, column);
                }
                comment.append('*');
            } else {
                comment.append(reader.current());
                reader.advance();
            }
        }

        errorHandler.addError(line, column, "Unterminated multi-line comment");
        return new Token(TokenType.INVALID, comment.toString(), line, column);
    }

    private boolean isLetter(char c) {
        return c >= 'a' && c <= 'z';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public int getTokenCount() {
        return tokenCount;
    }
}