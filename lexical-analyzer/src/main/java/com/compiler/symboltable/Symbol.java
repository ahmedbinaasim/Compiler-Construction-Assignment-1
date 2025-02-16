package com.compiler.symboltable;

import com.compiler.lexer.TokenType;

public class Symbol {
    private final String name;
    private final TokenType type;
    private final boolean isGlobal;
    private final boolean isConstant;
    private final String scope;
    private Object value;
    private final int line;
    private final int column;

    public Symbol(String name, TokenType type, boolean isGlobal, boolean isConstant, 
                 String scope, int line, int column) {
        this.name = name;
        this.type = type;
        this.isGlobal = isGlobal;
        this.isConstant = isConstant;
        this.scope = scope;
        this.line = line;
        this.column = column;
    }

    // Getters
    public String getName() { return name; }
    public TokenType getType() { return type; }
    public boolean isGlobal() { return isGlobal; }
    public boolean isConstant() { return isConstant; }
    public Object getValue() { return value; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public String getScope() { return scope; }

    // Setter for value with type checking
    public void setValue(Object value) throws IllegalStateException {
        if (isConstant && this.value != null) {
            throw new IllegalStateException("Cannot modify a constant value");
        }
        
        // Type checking
        if (value != null) {
            boolean isValid = switch(type) {
                case INT -> value instanceof Integer;
                case DEC -> value instanceof Double;
                case BOOL -> value instanceof Boolean;
                case CHAR -> value instanceof Character;
                case STR -> value instanceof String;
                default -> false;
            };
            
            if (!isValid) {
                throw new IllegalStateException(
                    String.format("Type mismatch: Cannot assign %s to %s", 
                        value.getClass().getSimpleName(), type));
            }
        }
        
        this.value = value;
    }

    @Override
    public String toString() {
        return String.format("Symbol{name='%s', type=%s, global=%b, constant=%b, scope='%s', value=%s, position=(%d,%d)}", 
            name, type, isGlobal, isConstant, scope, value, line, column);
    }
}