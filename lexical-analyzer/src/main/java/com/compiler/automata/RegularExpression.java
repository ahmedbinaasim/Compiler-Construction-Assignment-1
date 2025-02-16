package com.compiler.automata;

import com.compiler.error.ErrorHandler;

public class RegularExpression {
    private final String pattern;
    private final NFA nfa;
    private final DFA dfa;
    private final ErrorHandler errorHandler;

    public RegularExpression(String pattern, ErrorHandler errorHandler) {
        this.pattern = pattern;
        this.errorHandler = errorHandler;
        this.nfa = buildNFA();
        this.dfa = DFA.fromNFA(this.nfa, errorHandler);
    }

    private NFA buildNFA() {
        NFA nfa = new NFA(errorHandler);
        State startState = nfa.getStartState();
        State currentState = startState;
        
        try {
            int i = 0;
            while (i < pattern.length()) {
                char c = pattern.charAt(i);
                
                switch (c) {
                    case '(' -> {
                        i++; // Skip opening parenthesis
                        State groupStart = currentState;
                        State nextState = nfa.createState();
                        
                        // Process group
                        while (i < pattern.length() && pattern.charAt(i) != ')') {
                            if (pattern.charAt(i) == '|') {
                                currentState = groupStart;
                            } else {
                                State newState = nfa.createState();
                                nfa.addTransition(currentState, pattern.charAt(i), newState);
                                currentState = newState;
                            }
                            i++;
                        }
                        currentState = nextState;
                    }
                    case '*' -> {
                        State newState = nfa.createState();
                        nfa.addEpsilonTransition(currentState, startState);
                        nfa.addEpsilonTransition(startState, newState);
                        currentState = newState;
                    }
                    case '+' -> {
                        State newState = nfa.createState();
                        nfa.addEpsilonTransition(currentState, startState);
                        nfa.addTransition(currentState, pattern.charAt(i-1), newState);
                        currentState = newState;
                    }
                    case '?' -> {
                        State newState = nfa.createState();
                        nfa.addEpsilonTransition(currentState, newState);
                        currentState = newState;
                    }
                    case '|' -> {
                        State alternateFinal = nfa.createState();
                        nfa.addEpsilonTransition(currentState, alternateFinal);
                        currentState = startState;
                    }
                    case '[' -> {
                        i++; // Skip opening bracket
                        State rangeStart = currentState;
                        State rangeEnd = nfa.createState();
                        
                        boolean isNegated = pattern.charAt(i) == '^';
                        if (isNegated) i++;
                        
                        while (i < pattern.length() && pattern.charAt(i) != ']') {
                            if (pattern.charAt(i+1) == '-' && i+2 < pattern.length()) {
                                // Handle character range (e.g., a-z)
                                char start = pattern.charAt(i);
                                char end = pattern.charAt(i+2);
                                for (char ch = start; ch <= end; ch++) {
                                    if (!isNegated) {
                                        nfa.addTransition(rangeStart, ch, rangeEnd);
                                    }
                                }
                                i += 3;
                            } else {
                                if (!isNegated) {
                                    nfa.addTransition(rangeStart, pattern.charAt(i), rangeEnd);
                                }
                                i++;
                            }
                        }
                        currentState = rangeEnd;
                    }
                    case '\\' -> {
                        i++; // Skip backslash
                        if (i < pattern.length()) {
                            State newState = nfa.createState();
                            nfa.addTransition(currentState, pattern.charAt(i), newState);
                            currentState = newState;
                        }
                    }
                    default -> {
                        State newState = nfa.createState();
                        nfa.addTransition(currentState, c, newState);
                        currentState = newState;
                    }
                }
                i++;
            }
            
            currentState.setAccepting(true);
        } catch (Exception e) {
            errorHandler.addError(0, 0, "Error building NFA: " + e.getMessage());
        }
        
        return nfa;
    }

    public boolean matches(String input) {
        try {
            return dfa.accepts(input);
        } catch (Exception e) {
            errorHandler.addError(0, 0, "Error matching pattern: " + e.getMessage());
            return false;
        }
    }

    public void displayAutomataStates() {
        System.out.println("\nRegular Expression: " + pattern);
        System.out.println("======================");
        
        // Display NFA
        System.out.println("\nNFA Information:");
        System.out.println("Total NFA States: " + nfa.getStates().size());
        System.out.println("Unique NFA States: " + nfa.getUniqueStateCount());
        nfa.printTransitionTable();
        
        // Display DFA
        System.out.println("\nDFA Information:");
        System.out.println("Total DFA States: " + dfa.getStates().size());
        System.out.println("Unique DFA States: " + dfa.getUniqueStateCount());
        dfa.printTransitionTable();
    }

    public int getTotalStates() {
        return nfa.getStates().size() + dfa.getStates().size();
    }

    public int getUniqueStates() {
        return nfa.getUniqueStateCount() + dfa.getUniqueStateCount();
    }

    public void printNFATransitions() {
        nfa.printTransitionTable();
    }

    public void printDFATransitions() {
        dfa.printTransitionTable();
    }

    public String getPattern() {
        return pattern;
    }
    public NFA getNFA() {
        return nfa;
    }

    public DFA getDFA() {
        return dfa;
    }
}