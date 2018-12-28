package Model;

import java.util.ArrayList;
import java.util.Objects;

public class ClassTable {

    private String name;
    private String heritage;
    private VariableTable variables;
    private ArrayList<MethodTable> methods;

    public ClassTable(String name) {
        this.name = name;
        this.variables = new VariableTable();
        this.methods = new ArrayList();
    }

    public String getHeritage() {
        return heritage;
    }

    public VariableTable getVariables() {
        return variables;
    }

    public void setVariables(VariableTable variables) {
        this.variables = variables;
    }

    public ArrayList<MethodTable> getMethods() {
        return methods;
    }

    public String getName() {
        return name;
    }

    public void setHeritage(String heritage) {
        this.heritage = heritage;
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
        final ClassTable other = (ClassTable) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ClassTable{" + "name=" + name + ", heritage=" + heritage + ", variables=" + variables + ", methods=" + methods + '}';
    }
}
