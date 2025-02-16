package com.compiler.lexer;

public class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    // Getters
    public TokenType getType() { return type; }
    public String getValue() { return value; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    @Override
    public String toString() {
        return String.format("Token{type=%s, value='%s', position=(%d,%d)}", 
            type, value, line, column);
    }

    // Helper method to check if token is a specific type
    public boolean isType(TokenType type) {
        return this.type == type;
    }
}