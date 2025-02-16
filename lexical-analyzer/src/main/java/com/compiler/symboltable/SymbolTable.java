package com.compiler.symboltable;

import com.compiler.error.ErrorHandler;
import com.compiler.lexer.TokenType;
import java.util.*;

public class SymbolTable {
    private final Map<String, Stack<Symbol>> symbols;
    private final ErrorHandler errorHandler;
    private final Stack<String> scopes;
    private int currentScopeLevel;

    public SymbolTable(ErrorHandler errorHandler) {
        this.symbols = new HashMap<>();
        this.errorHandler = errorHandler;
        this.scopes = new Stack<>();
        this.currentScopeLevel = 0;
        enterScope("global");
    }

    public void enterScope(String scopeName) {
        scopes.push(scopeName);
        currentScopeLevel++;
    }

    public void exitScope() {
        if (currentScopeLevel > 0) {
            String scope = scopes.pop();
            // Remove all symbols from current scope
            for (Iterator<Map.Entry<String, Stack<Symbol>>> it = symbols.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Stack<Symbol>> entry = it.next();
                Stack<Symbol> stack = entry.getValue();
                while (!stack.isEmpty() && stack.peek().getScope().equals(scope)) {
                    stack.pop();
                }
                if (stack.isEmpty()) {
                    it.remove();
                }
            }
            currentScopeLevel--;
        }
    }

    public boolean insert(String name, TokenType type, boolean isGlobal, boolean isConstant, int line, int column) {
        // Check if the type is valid for variable declaration
        if (!isValidVariableType(type)) {
            errorHandler.addError(line, column, "Invalid type for variable declaration: " + type);
            return false;
        }

        // Check for redeclaration in current scope
        if (isSymbolInCurrentScope(name)) {
            errorHandler.addError(line, column, "Symbol '" + name + "' already declared in current scope");
            return false;
        }

        // Check global declaration in non-global scope
        if (isGlobal && !isGlobalScope()) {
            errorHandler.addError(line, column, "Global variables can only be declared in global scope");
            return false;
        }

        Symbol symbol = new Symbol(name, type, isGlobal, isConstant, getCurrentScope(), line, column);
        symbols.computeIfAbsent(name, k -> new Stack<>()).push(symbol);
        return true;
    }

    public Symbol lookup(String name) {
        Stack<Symbol> stack = symbols.get(name);
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return stack.peek();
    }

    public boolean updateValue(String name, Object value, int line, int column) {
        Symbol symbol = lookup(name);
        if (symbol == null) {
            errorHandler.addError(line, column, "Symbol '" + name + "' not declared");
            return false;
        }

        try {
            symbol.setValue(value);
            return true;
        } catch (IllegalStateException e) {
            errorHandler.addError(line, column, e.getMessage());
            return false;
        }
    }

    public String getCurrentScope() {
        return scopes.peek();
    }

    public boolean isGlobalScope() {
        return currentScopeLevel == 1 && "global".equals(getCurrentScope());
    }

    private boolean isSymbolInCurrentScope(String name) {
        Stack<Symbol> stack = symbols.get(name);
        return stack != null && !stack.isEmpty() && 
               stack.peek().getScope().equals(getCurrentScope());
    }

    public void printSymbols() {
        System.out.println("\nSymbol Table Contents:");
        System.out.println("---------------------");
        for (Map.Entry<String, Stack<Symbol>> entry : symbols.entrySet()) {
            System.out.println("Identifier: " + entry.getKey());
            entry.getValue().forEach(symbol -> 
                System.out.println("  " + symbol)
            );
        }
        System.out.println("---------------------\n");
    }

    public boolean isTypeCompatible(TokenType variableType, TokenType valueType) {
        if (variableType == valueType) {
            return true;
        }

        return switch (variableType) {
            case DEC -> valueType == TokenType.INTEGER_LITERAL || 
                       valueType == TokenType.DECIMAL_LITERAL;
            case INT -> valueType == TokenType.INTEGER_LITERAL;
            case BOOL -> valueType == TokenType.BOOLEAN_LITERAL;
            case CHAR -> valueType == TokenType.CHARACTER_LITERAL;
            case STR -> valueType == TokenType.STRING_LITERAL;
            default -> false;
        };
    }

    public boolean isValidVariableType(TokenType type) {
        return type == TokenType.INT || 
               type == TokenType.DEC || 
               type == TokenType.BOOL || 
               type == TokenType.CHAR || 
               type == TokenType.STR;
    }

    // Helper methods for debug and testing
    public int getCurrentScopeLevel() {
        return currentScopeLevel;
    }

    public List<Symbol> getAllSymbols() {
        List<Symbol> allSymbols = new ArrayList<>();
        symbols.values().forEach(stack -> allSymbols.addAll(stack));
        return allSymbols;
    }
}