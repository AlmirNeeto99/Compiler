package Model;

public class Token {

    private String lexeme;
    private Attribute attr_name;
    private int line;

    public Token(String lexeme, Attribute attr_name, int line) {
        this.lexeme = lexeme;
        this.attr_name = attr_name;
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public String getLexeme() {
        return lexeme;
    }

    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    public Attribute getAttr_name() {
        return attr_name;
    }

    public void setAttr_name(Attribute attr_name) {
        this.attr_name = attr_name;
    }

    @Override
    public String toString() {
        return "<" + attr_name + ", " + lexeme +", Line: "+line+ ">";
    }

}
