package com.compiler.lexer;

public enum TokenType {
    // Keywords
    INT,
    DEC,
    BOOL,
    CHAR,
    STR,
    IN,
    OUT,
    OUTLN,
    
    // Literals
    INTEGER_LITERAL,
    DECIMAL_LITERAL,
    BOOLEAN_LITERAL,
    CHARACTER_LITERAL,
    STRING_LITERAL,
    
    // Operators
    PLUS,          // +
    MINUS,         // -
    MULTIPLY,      // *
    DIVIDE,        // /
    MODULUS,       // %
    EXPONENT,      // ^
    ASSIGN,        // =
    
    // Comparators
    LESS_THAN,     // <
    GREATER_THAN,  // >
    EQUALS,        // ==
    NOT_EQUALS,    // !=
    
    // Symbols
    SEMICOLON,     // ;
    GLOBAL,        // @
    
    // Comments
    SINGLE_COMMENT,    // #
    MULTI_COMMENT_START, // #*
    MULTI_COMMENT_END,   // *#
    
    // Others
    IDENTIFIER,
    EOF,
    
    // Special
    INVALID
}