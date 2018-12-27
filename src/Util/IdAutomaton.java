package Util;

import Model.AlphabetSet;
import Model.State;
import java.util.HashSet;

public class IdAutomaton{

    private State actual_state;
    private final AlphabetSet alpha = new AlphabetSet();
    private final HashSet<Integer> validLetters;
    private final HashSet<Integer> validNumbers;
    private final int underLine = (int) '_';
    private final State[] states = new State[2];
    
    private boolean canBeValid = true;
    private int transitions = 0;
    

    public IdAutomaton() {
        this.validLetters = alpha.getLetters();
        this.validNumbers = alpha.getNumbers();
        this.states[0] = new State("q0", false);
        this.states[1] = new State("q1", true);
        this.actual_state = states[0];
    }
    
    public void testChar(char character){
        int c = (int)character;
        
        if(actual_state.equals(states[0]) && validLetters.contains(c)){
            actual_state = states[1];
            transitions++;
            return;
        }
        else if(actual_state.equals(states[1]) && validLetters.contains(c) || actual_state.equals(states[1]) && validNumbers.contains(c) || actual_state.equals(states[1]) && c == underLine){
            actual_state = states[1];
            transitions++;
            return;
        }
        this.canBeValid = false;
    }
    public boolean canBeValid(){
        return this.canBeValid;
    }
    
    public int transition(){
        return this.transitions;
    }
    
    public void reset(){
        this.actual_state = states[0];
        this.canBeValid = true;
        this.transitions = 0;
    }  

    public State getActual_state() {
        return this.actual_state;
    }
    
}
