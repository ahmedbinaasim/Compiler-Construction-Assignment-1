package com.compiler.automata;

import java.util.*;

public class State {
    private final int id;
    private boolean isAccepting;
    private final Map<Character, Set<State>> transitions;
    private final Set<State> epsilonTransitions;

    public State(int id) {
        this.id = id;
        this.isAccepting = false;
        this.transitions = new HashMap<>();
        this.epsilonTransitions = new HashSet<>();
    }

    public void addTransition(char symbol, State state) {
        transitions.computeIfAbsent(symbol, k -> new HashSet<>()).add(state);
    }

    public void addEpsilonTransition(State state) {
        epsilonTransitions.add(state);
    }

    public Set<State> getTransitions(char symbol) {
        return transitions.getOrDefault(symbol, new HashSet<>());
    }

    public Set<State> getEpsilonTransitions() {
        return epsilonTransitions;
    }

    public Map<Character, Set<State>> getAllTransitions() {
        return new HashMap<>(transitions);
    }

    public int getId() {
        return id;
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public void setAccepting(boolean accepting) {
        isAccepting = accepting;
    }

    public Set<State> getEpsilonClosure() {
        Set<State> closure = new HashSet<>();
        Stack<State> stack = new Stack<>();
        stack.push(this);
        
        while (!stack.isEmpty()) {
            State current = stack.pop();
            if (closure.add(current)) {
                stack.addAll(current.getEpsilonTransitions());
            }
        }
        
        return closure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return id == state.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "q" + id;
    }
}