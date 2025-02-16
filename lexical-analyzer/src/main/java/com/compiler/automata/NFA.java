package com.compiler.automata;

import com.compiler.error.ErrorHandler;
import java.util.*;

public class NFA {
    private final State startState;
    private final Set<State> states;
    private final Set<Character> alphabet;
    private static int stateCounter = 0;
    private final ErrorHandler errorHandler;
    private final Set<State> uniqueStates;  // Track unique states for each parse

    public NFA(ErrorHandler errorHandler) {
        this.states = new HashSet<>();
        this.alphabet = new HashSet<>();
        this.errorHandler = errorHandler;
        this.uniqueStates = new HashSet<>();
        this.startState = createState();
    }

    public State createState() {
        State state = new State(stateCounter++);
        states.add(state);
        uniqueStates.add(state);
        return state;
    }

    public void addTransition(State from, char symbol, State to) {
        if (!states.contains(from) || !states.contains(to)) {
            errorHandler.addError(0, 0, "Invalid state in transition");
            return;
        }
        alphabet.add(symbol);
        from.addTransition(symbol, to);
    }

    public void addEpsilonTransition(State from, State to) {
        if (!states.contains(from) || !states.contains(to)) {
            errorHandler.addError(0, 0, "Invalid state in epsilon transition");
            return;
        }
        from.addEpsilonTransition(to);
    }

    public Set<State> getStates() {
        return states;
    }

    public State getStartState() {
        return startState;
    }

    public Set<Character> getAlphabet() {
        return alphabet;
    }

    public int getUniqueStateCount() {
        return uniqueStates.size();
    }

    public Set<State> getNextStates(State current, char symbol) {
        if (!states.contains(current)) {
            errorHandler.addError(0, 0, "Invalid state in getNextStates");
            return new HashSet<>();
        }
        
        Set<State> result = new HashSet<>();
        Set<State> epsilonStates = current.getEpsilonClosure();
        
        for (State state : epsilonStates) {
            result.addAll(state.getTransitions(symbol));
        }
        
        Set<State> nextEpsilonStates = new HashSet<>();
        for (State state : result) {
            nextEpsilonStates.addAll(state.getEpsilonClosure());
        }
        
        result.addAll(nextEpsilonStates);
        uniqueStates.addAll(result);  // Track unique states
        return result;
    }

    public boolean accepts(String input) {
        Set<State> currentStates = startState.getEpsilonClosure();
        uniqueStates.addAll(currentStates);  // Track initial states
        
        for (char c : input.toCharArray()) {
            Set<State> nextStates = new HashSet<>();
            for (State state : currentStates) {
                nextStates.addAll(getNextStates(state, c));
            }
            currentStates = nextStates;
            uniqueStates.addAll(nextStates);  // Track states for each character
        }
        
        return currentStates.stream().anyMatch(State::isAccepting);
    }

    public void reset() {
        stateCounter = 0;
        uniqueStates.clear();
    }

    public void printTransitionTable() {
        System.out.println("\nNFA Transition Table:");
        System.out.println("--------------------");
        
        // Print header
        System.out.print("State\tε\t");
        List<Character> sortedAlphabet = new ArrayList<>(alphabet);
        Collections.sort(sortedAlphabet);
        for (char c : sortedAlphabet) {
            System.out.print(c + "\t");
        }
        System.out.println();
        
        // Print transitions for each state
        List<State> sortedStates = new ArrayList<>(states);
        sortedStates.sort(Comparator.comparingInt(State::getId));
        
        for (State state : sortedStates) {
            System.out.print(state + "\t");
            
            // Print epsilon transitions
            Set<State> epsilonTrans = state.getEpsilonTransitions();
            System.out.print(epsilonTrans.isEmpty() ? "-\t" : epsilonTrans + "\t");
            
            // Print transitions for each symbol
            for (char c : sortedAlphabet) {
                Set<State> trans = state.getTransitions(c);
                System.out.print(trans.isEmpty() ? "-\t" : trans + "\t");
            }
            
            // Mark accepting states
            if (state.isAccepting()) {
                System.out.print("(accepting)");
            }
            System.out.println();
        }
        
        System.out.println("Total States: " + states.size());
        System.out.println("Unique States in Current Parse: " + uniqueStates.size());
        System.out.println("--------------------");
    }
}