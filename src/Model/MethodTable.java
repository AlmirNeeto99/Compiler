package Model;

import java.util.ArrayList;

public class MethodTable {

    private VariableTable variables;
    private String type;
    private ArrayList<String[]> parameters;
    private ArrayList<String[]> returns;
    
    public MethodTable(){
        this.variables = new VariableTable();
        this.parameters = new ArrayList();
        this.returns = new ArrayList();
    }

    public VariableTable getVariables() {
        return variables;
    }
    public String getType() {
        return type;
    }
    public ArrayList<String[]> getParameters() {
        return parameters;
    }
    public ArrayList<String[]> getReturns() {
        return returns;
    }
}
