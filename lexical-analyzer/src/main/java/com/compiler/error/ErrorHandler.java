package com.compiler.error;

import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
    private final List<CompilerError> errors;
    private final List<String> warnings;
    
    public ErrorHandler() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    public void addError(int line, int column, String message) {
        errors.add(new CompilerError(line, column, message, ErrorType.ERROR));
    }

    public void addWarning(int line, int column, String message) {
        warnings.add(String.format("Warning at line %d, column %d: %s", line, column, message));
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
    
    public void printErrors() {
        if (!errors.isEmpty()) {
            System.err.println("\nErrors:");
            System.err.println("-------");
            for (CompilerError error : errors) {
                System.err.println(error);
            }
        }
        
        if (!warnings.isEmpty()) {
            System.err.println("\nWarnings:");
            System.err.println("---------");
            for (String warning : warnings) {
                System.err.println(warning);
            }
        }
    }

    public void printErrorCount() {
        System.err.println(String.format("\nFound %d error(s) and %d warning(s)", 
            errors.size(), warnings.size()));
    }
    
    public void clear() {
        errors.clear();
        warnings.clear();
    }
    
    public List<CompilerError> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<String> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public boolean canContinue() {
        return errors.isEmpty();
    }
    
    private enum ErrorType {
        ERROR,
        WARNING
    }
    
    private static class CompilerError {
        private final int line;
        private final int column;
        private final String message;
        private final ErrorType type;
        
        public CompilerError(int line, int column, String message, ErrorType type) {
            this.line = line;
            this.column = column;
            this.message = message;
            this.type = type;
        }
        
        @Override
        public String toString() {
            return String.format("%s at line %d, column %d: %s", 
                type.name(), line, column, message);
        }
    }
}