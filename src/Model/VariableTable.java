package Model;

import java.util.ArrayList;

public class VariableTable {

    private ArrayList<String[]> variables;
    
    public VariableTable(){
        this.variables = new ArrayList();
    }

    public ArrayList<String[]> getVariables() {
        return variables;
    }
    public void setVariables(ArrayList<String[]> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "VariableTable{" + "variables=" + variables + '}';
    }
}
