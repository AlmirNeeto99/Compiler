package Util;

import Model.AlphabetSet;
import Model.State;
import java.util.HashSet;

public class NumberAutomaton extends AlphabetSet{
    
    private State actual_state;
    private final HashSet<Integer> validNumbers;
    private final int minus = (int) '-';
    private final int dot = (int)'.';
    private final State[] states = new State[5];
    
    private boolean canBeValid = true;
    
    private int transitions;
    
    public NumberAutomaton(){
        super();
        this.validNumbers = AlphabetSet.getNumbers();
        this.states[0] = new State("q0", false);
        this.states[1] = new State("q1", false);
        this.states[2] = new State("q2", true);
        this.states[3] = new State("q3", false);
        this.states[4] = new State("q4", true);
        this.actual_state = states[0];
    }
    
    public void testChar(char character){
        int c = (int)character;
        
        if(actual_state.equals(states[0]) && c == minus){
            actual_state = states[1];
            transitions++;
            return;
        }
        else if(actual_state.equals(states[0]) && validNumbers.contains(c)){
            actual_state = states[2];
            transitions++;
            return;
        }
        else if(actual_state.equals(states[1]) && validNumbers.contains(c)){
            actual_state = states[2];
            transitions++;
            return;
        }
        else if(actual_state.equals(states[2]) && validNumbers.contains(c)){
            transitions++;
            return;
        }
        else if(actual_state.equals(states[2]) && c == dot){
            actual_state = states[3];
            transitions++;
            return;
        }
        else if(actual_state.equals(states[3]) && validNumbers.contains(c)){
            actual_state = states[4];
            transitions++;
            return;
        }
        else if(actual_state.equals(states[4]) && validNumbers.contains(c)){
            transitions++;
            return;
        }
        this.canBeValid = false;
    }
    
    public boolean canBeValid(){
        return this.canBeValid;
    }
    
    public int transitions(){
        return this.transitions;
    }
    
    public void reset(){
        this.canBeValid = true;
        this.actual_state = states[0];
        this.transitions = 0;
    }
    
    public State getActualState(){
        return this.actual_state;
    }

}
