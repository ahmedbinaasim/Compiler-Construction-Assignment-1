package com.compiler.automata;

import java.util.*;

public class AutomataVisualizer {
    @SuppressWarnings("unused")
    private static final int MAX_WIDTH = 80;
    private static final String ARROW = "-->";
    private static final String BRANCH = "├";
    private static final String CORNER = "└";
    @SuppressWarnings("unused")
    private static final String VERTICAL = "│";

    public static void visualizeAutomata(RegularExpression regex) {
        System.out.println("\nPattern: " + regex.getPattern());
        System.out.println("=".repeat(40));
        
        // Get NFA and DFA from regex
        @SuppressWarnings("unused")
        NFA nfa = regex.getNFA();
        DFA dfa = regex.getDFA();
        
        // Draw state diagram
        drawStateDiagram(dfa);
        
        // Show legend
        printLegend();
        
        // Start interactive testing
        startInteractiveTesting(dfa);
    }

    private static void drawStateDiagram(DFA dfa) {
        System.out.println("\nState Diagram:");
        System.out.println("[START] " + ARROW + " (q0)");
        
        Map<Character, Set<State>> transitions = new HashMap<>();
        for (State state : dfa.getStates()) {
            for (Map.Entry<Character, Set<State>> entry : state.getAllTransitions().entrySet()) {
                transitions.computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                          .addAll(entry.getValue());
            }
        }

        // Draw transitions
        boolean isFirst = true;
        for (Map.Entry<Character, Set<State>> entry : transitions.entrySet()) {
            String prefix = isFirst ? BRANCH : CORNER;
            System.out.printf("%s%s--%c%s ((q%d))\n",
                " ".repeat(8), prefix, entry.getKey(), ARROW,
                entry.getValue().iterator().next().getId());
            isFirst = false;
        }
    }

    private static void printLegend() {
        System.out.println("\nLegend:");
        System.out.println("(q0)    - Regular State");
        System.out.println("((q1))  - Accepting State");
        System.out.println("--a-->  - Transition on character 'a'");
        System.out.println("[START] - Initial State");
    }

    private static void startInteractiveTesting(DFA dfa) {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\nInteractive Testing Mode:");
            System.out.println("-----------------------");
            System.out.print("Enter a string to test (or 'exit' to quit): ");
            
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            System.out.println("Processing '" + input + "':");
            State currentState = dfa.getStartState();
            boolean isAccepted = true;

            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                Set<State> nextStates = currentState.getTransitions(c);
                
                if (nextStates.isEmpty()) {
                    System.out.printf("%c: q%d -> ✗ (rejected)\n", 
                        c, currentState.getId());
                    isAccepted = false;
                    break;
                } else {
                    State nextState = nextStates.iterator().next();
                    System.out.printf("%c: q%d -> q%d (%s)\n", 
                        c, currentState.getId(), nextState.getId(),
                        nextState.isAccepting() ? "✓" : "continuing");
                    currentState = nextState;
                }
            }

            if (isAccepted) {
                System.out.printf("\nResult: %s %s\n", 
                    currentState.isAccepting() ? "ACCEPTED" : "REJECTED",
                    currentState.isAccepting() ? "✓" : "✗");
            }

            System.out.print("\nTry another? (y/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
                break;
            }
            // scanner.close();
        }
    }
}