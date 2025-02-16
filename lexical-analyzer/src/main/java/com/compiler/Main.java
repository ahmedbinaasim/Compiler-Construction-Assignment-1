package com.compiler;

import com.compiler.error.ErrorHandler;
import com.compiler.lexer.LexicalAnalyzer;
import com.compiler.lexer.Token;
import com.compiler.lexer.TokenType;
import com.compiler.symboltable.SymbolTable;
import com.compiler.automata.AutomataVisualizer;
import com.compiler.automata.RegularExpression;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ErrorHandler errorHandler = new ErrorHandler();
        SymbolTable symbolTable = new SymbolTable(errorHandler);

        while (true) {
            System.out.println("\nAA Language Compiler");
            System.out.println("===================");
            System.out.println("1. Analyze a .aa file");
            System.out.println("2. Enter code directly");
            System.out.println("3. View automata states");
            System.out.println("4. View symbol table");
            System.out.println("5. Exit");
            System.out.print("\nChoose an option: ");

            try {
                int choice = Integer.parseInt(scanner.nextLine());
                switch (choice) {
                    case 1 -> analyzeFile(scanner, errorHandler, symbolTable);
                    case 2 -> analyzeInput(scanner, errorHandler, symbolTable);
                    case 3 -> viewAutomataStates(errorHandler);
                    case 4 -> viewSymbolTable(symbolTable);
                    case 5 -> {
                        System.out.println("Exiting compiler...");
                        return;
                    }
                    default -> System.out.println("Invalid option!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }

    private static void analyzeFile(Scanner scanner, ErrorHandler errorHandler, SymbolTable symbolTable) {
        System.out.print("Enter file path (.aa file): ");
        String filePath = scanner.nextLine();

        if (!filePath.endsWith(".aa")) {
            System.err.println("Error: File must have .aa extension");
            return;
        }

        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                System.err.println("Error: File does not exist");
                return;
            }

            String content = Files.readString(path);
            processCode(content, errorHandler, symbolTable);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static void analyzeInput(Scanner scanner, ErrorHandler errorHandler, SymbolTable symbolTable) {
        System.out.println("Enter your AA code (type 'END' on a new line to finish):");
        System.out.println("---------------------------------------------------");
        
        StringBuilder code = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            code.append(line).append("\n");
        }
        
        processCode(code.toString(), errorHandler, symbolTable);
    }

    private static void processCode(String code, ErrorHandler errorHandler, SymbolTable symbolTable) {
    System.out.println("\nCompilation Process:");
    System.out.println("===================");
    
    // Phase 1: Lexical Analysis
    System.out.println("\n1. Lexical Analysis");
    System.out.println("------------------");
    LexicalAnalyzer lexer = new LexicalAnalyzer(code, errorHandler);
    List<Token> tokens = lexer.tokenize();
    
    System.out.println("Total tokens found: " + lexer.getTokenCount());
    
    if (errorHandler.hasErrors()) {
        System.out.println("\nLexical Errors Found:");
        errorHandler.printErrors();
        return;
    }

    // Print tokens if no errors
    System.out.println("\nTokens:");
    for (Token token : tokens) {
        System.out.println(token);
    }
    
    // Phase 2: Symbol Table Population
    System.out.println("\n2. Symbol Table Construction");
    System.out.println("--------------------------");
    
    boolean isGlobal = false;
    TokenType currentType = null;
    
    for (int i = 0; i < tokens.size(); i++) {
        Token token = tokens.get(i);
        
        if (token.getType() == TokenType.GLOBAL) {
            isGlobal = true;
            continue;
        }
        
        // Check for type declarations
        if (token.getType() == TokenType.INT || 
            token.getType() == TokenType.DEC || 
            token.getType() == TokenType.BOOL || 
            token.getType() == TokenType.CHAR || 
            token.getType() == TokenType.STR) {
            currentType = token.getType();
            continue;
        }
        
        // Handle identifier declarations
        if (currentType != null && token.getType() == TokenType.IDENTIFIER) {
            symbolTable.insert(token.getValue(), currentType, isGlobal, false,
                             token.getLine(), token.getColumn());
            currentType = null;
            isGlobal = false;
        }
    }
    
    // Print final compilation status
    if (errorHandler.hasErrors()) {
        System.out.println("\nSymbol Table Errors Found:");
        errorHandler.printErrors();
    } else {
        System.out.println("\nCompilation Summary:");
        System.out.println("===================");
        System.out.println("Tokens processed: " + lexer.getTokenCount());
        System.out.println("Symbols defined: " + symbolTable.getAllSymbols().size());
    }
    
    if (errorHandler.hasWarnings()) {
        System.out.println("\nWarnings:");
        errorHandler.printErrors();
    }
}

    private static void viewAutomataStates(ErrorHandler errorHandler) {
        System.out.println("\nAutomata Visualization");
        System.out.println("=====================");
        
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nSelect pattern to visualize:");
            System.out.println("1. Identifier Pattern ([a-z]+)");
            System.out.println("2. Number Pattern ([0-9]+)");
            System.out.println("3. Back to main menu");
            System.out.print("\nChoice: ");
            
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                RegularExpression pattern = null;
                
                switch (choice) {
                    case 1 -> {
                        pattern = new RegularExpression(
                            "(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)+",
                            errorHandler
                        );
                    }
                    case 2 -> {
                        pattern = new RegularExpression(
                            "(0|1|2|3|4|5|6|7|8|9)+",
                            errorHandler
                        );
                    }
                    case 3 -> {
                        return;
                    }
                    default -> {
                        System.out.println("Invalid choice!");
                        continue;
                    }
                }
                
                if (pattern != null) {
                    AutomataVisualizer.visualizeAutomata(pattern);
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number!");
            }
        }
    }

    private static void viewSymbolTable(SymbolTable symbolTable) {
        System.out.println("\nCurrent Symbol Table");
        System.out.println("===================");
        symbolTable.printSymbols();
    }
}