
package Model;

import java.util.Objects;

public class State {
    
    private final boolean isFinal;
    private final String name;
    
    public State(String name, boolean isFinal){
        this.name = name;
        this.isFinal = isFinal;
    }
    
    public boolean isFinal(){
        return this.isFinal;
    }

    public String getName() {
        return name;
    }
    

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final State other = (State) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "State{" + "name=" + name + '}';
    }
    
    
    
    
}
