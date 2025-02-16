package com.compiler.automata;

import com.compiler.error.ErrorHandler;
import java.util.*;

public class DFA {
    private final State startState;
    private final Set<State> states;
    private final Set<Character> alphabet;
    private static int stateCounter = 0;
    private final ErrorHandler errorHandler;
    private final Set<State> uniqueStates;

    public DFA(ErrorHandler errorHandler) {
        this.states = new HashSet<>();
        this.alphabet = new HashSet<>();
        this.errorHandler = errorHandler;
        this.uniqueStates = new HashSet<>();
        this.startState = createState();
    }

    private State createState() {
        State state = new State(stateCounter++);
        states.add(state);
        uniqueStates.add(state);
        return state;
    }

    public static DFA fromNFA(NFA nfa, ErrorHandler errorHandler) {
        DFA dfa = new DFA(errorHandler);
        Map<Set<State>, State> dfaStates = new HashMap<>();
        Queue<Set<State>> unprocessedStates = new LinkedList<>();
        
        try {
            // Start with epsilon closure of NFA's start state
            Set<State> startStateSet = nfa.getStartState().getEpsilonClosure();
            State dfaStartState = dfa.createState();
            dfaStates.put(startStateSet, dfaStartState);
            unprocessedStates.add(startStateSet);
            
            dfaStartState.setAccepting(startStateSet.stream().anyMatch(State::isAccepting));
            
            while (!unprocessedStates.isEmpty()) {
                Set<State> currentStateSet = unprocessedStates.poll();
                State currentDFAState = dfaStates.get(currentStateSet);
                
                for (char symbol : nfa.getAlphabet()) {
                    Set<State> nextStateSet = new HashSet<>();
                    
                    for (State nfaState : currentStateSet) {
                        Set<State> nextStates = nfa.getNextStates(nfaState, symbol);
                        nextStateSet.addAll(nextStates);
                    }
                    
                    if (nextStateSet.isEmpty()) {
                        continue;
                    }
                    
                    State nextDFAState = dfaStates.get(nextStateSet);
                    if (nextDFAState == null) {
                        nextDFAState = dfa.createState();
                        dfaStates.put(nextStateSet, nextDFAState);
                        unprocessedStates.add(nextStateSet);
                        
                        nextDFAState.setAccepting(
                            nextStateSet.stream().anyMatch(State::isAccepting)
                        );
                    }
                    
                    dfa.addTransition(currentDFAState, symbol, nextDFAState);
                }
            }
        } catch (Exception e) {
            errorHandler.addError(0, 0, "Error during NFA to DFA conversion: " + e.getMessage());
        }
        
        return dfa;
    }

    public void addTransition(State from, char symbol, State to) {
        if (!states.contains(from) || !states.contains(to)) {
            errorHandler.addError(0, 0, "Invalid state in transition");
            return;
        }
        alphabet.add(symbol);
        from.addTransition(symbol, to);
    }

    public Set<State> getStates() {
        return states;
    }

    public State getStartState() {
        return startState;
    }

    public int getUniqueStateCount() {
        return uniqueStates.size();
    }

    public boolean accepts(String input) {
        State currentState = startState;
        uniqueStates.add(currentState);
        
        for (char c : input.toCharArray()) {
            Set<State> nextStates = currentState.getTransitions(c);
            if (nextStates.isEmpty()) {
                return false;
            }
            currentState = nextStates.iterator().next();
            uniqueStates.add(currentState);
        }
        
        return currentState.isAccepting();
    }

    public void printTransitionTable() {
        System.out.println("\nDFA Transition Table:");
        System.out.println("--------------------");
        
        List<Character> sortedAlphabet = new ArrayList<>(alphabet);
        Collections.sort(sortedAlphabet);
        List<State> sortedStates = new ArrayList<>(states);
        sortedStates.sort(Comparator.comparingInt(State::getId));
        
        // Print header
        System.out.print("State\t");
        for (char c : sortedAlphabet) {
            System.out.print(c + "\t");
        }
        System.out.println("Accept?");
        
        // Print transitions
        for (State state : sortedStates) {
            System.out.print(state + "\t");
            
            for (char c : sortedAlphabet) {
                Set<State> trans = state.getTransitions(c);
                System.out.print(trans.isEmpty() ? "-\t" : trans.iterator().next() + "\t");
            }
            
            System.out.println(state.isAccepting() ? "Yes" : "No");
        }
        
        System.out.println("Total States: " + states.size());
        System.out.println("Unique States in Current Parse: " + uniqueStates.size());
        System.out.println("--------------------");
    }

    public void reset() {
        stateCounter = 0;
        uniqueStates.clear();
    }
}