package Model;

import java.util.ArrayList;

public class SyntaticAnalyzer {

    private ArrayList<Token> tokens = new ArrayList<>();
    private Token token;
    private int pos;

    public SyntaticAnalyzer() {
        this.pos = 0;
    }

    public void analyze(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.token = tokens.get(pos++);
        start();
    }

    public void start() {
        constantDeclaration();
        classDeclaration();
        moreClasses();
        if(pos == tokens.size()){
            System.out.println("Success");
        }
    }

    private void constantDeclaration() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("const")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                getToken();
                constants();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                    getToken();
                }
            }
        }
    }

    private void classDeclaration() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("class")) {
            getToken();
            classIdentification();
        }
    }

    private void moreClasses() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("class")) {
            classDeclaration();
            moreClasses();
        }
    }

    private void classIdentification() {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            classHeritage();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                getToken();
                classBody();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                    getToken();
                }
            }
        }
    }

    private void classHeritage() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("extends")) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
            }
        }
    }

    private void classBody() {
        classAtributes();
        classMethods();
    }

    private void classAtributes() {
        variableDeclaration();
    }

    private void variableDeclaration() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("variables")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                getToken();
                variable();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                    getToken();
                }
            }
        }
    }

    private void constants() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int") || token.getLexeme().equals("void"))) {
            getToken();
            constantAttribution();
            moreConstants();
        }
    }

    private void moreConstants() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            constantAttribution();
            moreConstants();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
            getToken();
            newDeclaration();
        }
    }

    private void newDeclaration() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int") || token.getLexeme().equals("void"))) {
            constants();
        }
    }

    private void constantAttribution() {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            if (token.getLexeme().equals("=")) {
                getToken();
                value();
            }
        }
    }

    private void value() {
        if (token.getAttr_name() == Attribute.STRING || token.getAttr_name() == Attribute.NUMBER || token.getLexeme().equals("true") || token.getLexeme().equals("false")) {
            getToken();
        }
    }

    private void classMethods() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            methodDeclaration();
        }
    }

    private void methodDeclaration() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            getToken();
            type();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                    getToken();
                    parameterDeclaration();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                            getToken();
                            variablesDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                                getToken();
                                moreMethods();
                            }
                        }
                    }
                }
            }
        }
    }

    private void commands() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("if")) {
            ifStatement();
            commands();
        } else if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("while")) {
            whileStatement();
            commands();
        } else if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("read")) {
            readStatement();
            commands();
        } else if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("write")) {
            writeStatement();
            commands();
        } else if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("return")) {
            return1();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
                getToken();
            }
        } else if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            attribution();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
                getToken();
            }
            commands();
        } else if (token.getAttr_name() == Attribute.ID) {
            attribution();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
                getToken();
            }
            commands();
        }
    }

    private void attribution() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            increment();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                arrayVerification();
                attr();
            }
        } else if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attr();
            verif();
        }
    }

    private void verif() {
        if (token.getAttr_name() == Attribute.REL_OP && token.getLexeme().equals("=")) {
            normalAttribution2();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            complement();
        }
    }

    private void complement() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            getToken();
            param();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                getToken();
            }
        }
    }

    private void param() {
        if (token.getAttr_name() == Attribute.STRING) {
            getToken();
            moreParam();
        }else if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("true") || token.getLexeme().equals("false"))) {
            expression();
            moreParam();
        } else if (token.getAttr_name() == Attribute.LOGICAL_OP && token.getLexeme().equals("!")) {
            expression();
            moreParam();
        } else if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--") || token.getLexeme().equals("-"))) {
            expression();
            moreParam();
        } else if (token.getAttr_name() == Attribute.NUMBER || token.getAttr_name() == Attribute.ID) {
            expression();
            moreParam();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            expression();
            moreParam();
        }
    }

    private void param2() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            complement();
        }
    }

    private void moreParam() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            obrigatoryParam();
        }
    }

    private void obrigatoryParam() {
        if (token.getAttr_name() == Attribute.STRING) {
            getToken();
            moreParam();
        }else if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("true") || token.getLexeme().equals("false"))) {
            expression();
            moreParam();
        } else if (token.getAttr_name() == Attribute.LOGICAL_OP && token.getLexeme().equals("!")) {
            expression();
            moreParam();
        } else if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--") || token.getLexeme().equals("-"))) {
            expression();
            moreParam();
        } else if (token.getAttr_name() == Attribute.NUMBER || token.getAttr_name() == Attribute.ID) {
            expression();
            moreParam();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            expression();
            moreParam();
        }
    }

    private void normalAttribution2() {
        if (token.getAttr_name() == Attribute.REL_OP && token.getLexeme().equals("=")) {
            getToken();
            normalAttribution3();
        } else if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            increment();
        }
    }

    private void normalAttribution3() {
        if (token.getAttr_name() == Attribute.STRING) {
            getToken();
        } else if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("true") || token.getLexeme().equals("false"))) {
            expression();
        } else if (token.getAttr_name() == Attribute.LOGICAL_OP && token.getLexeme().equals("!")) {
            expression();
        } else if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--") || token.getLexeme().equals("-"))) {
            expression();
        } else if (token.getAttr_name() == Attribute.NUMBER || token.getAttr_name() == Attribute.ID) {
            expression();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            expression();
        }
    }

    private void expression() {
        addExp();
        relationalExp();
    }

    private void relationalExp() {
        if (isRelationalOperator()) {
            getToken();
            addExp();
            logicalExp();
        } else if (token.getAttr_name() == Attribute.LOGICAL_OP && (token.getLexeme().equals("||") || token.getLexeme().equals("&&"))) {
            logicalExp();
        }
    }

    private void logicalExp() {
        if (token.getAttr_name() == Attribute.LOGICAL_OP && (token.getLexeme().equals("||") || token.getLexeme().equals("&&"))) {
            getToken();
            expression();
        }
    }

    private void addExp() {
        multExp();
        d();
    }

    private void d() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("+") || token.getLexeme().equals("-"))) {
            getToken();
            addExp();
        }
    }

    private void e() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("*") || token.getLexeme().equals("/"))) {
            getToken();
            multExp();
        }
    }

    private void multExp() {
        negExp();
        e();
    }
    
    private void negExp(){
        if(token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("-") || token.getLexeme().equals("++")||token.getLexeme().equals("--"))){
            getToken();
            expValue();
        }
        else if(token.getAttr_name() == Attribute.LOGICAL_OP && token.getLexeme().equals("!")){
            getToken();
            expValue();
        }
        else if(token.getAttr_name() == Attribute.NUMBER || token.getAttr_name() == Attribute.ID){
            expValue();
            g();
        }
        else if(token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("true") || token.getLexeme().equals("false"))){
            expValue();
            g();
        }
        else if(token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")){
            expValue();
            g();
        }
    }
    
    private void expValue(){
        if(token.getAttr_name() == Attribute.NUMBER){
            getToken();
        }
        else if(token.getAttr_name() == Attribute.ID){
            getToken();
            arrayVerification();
            attr();
            param2();
        }
        else if(token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("true") || token.getLexeme().equals("false"))){
            getToken();
        }
        else if(token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")){
            getToken();
            expression();
            if(token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")){
                getToken();
            }
        }
    }
    
    private void g(){
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            getToken();
        }
    }

    private void increment() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            getToken();
        }
    }

    private void writeStatement() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("write")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                writing1();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                    getToken();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
                        getToken();
                    }
                }
            }
        }
    }

    private void writing1() {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attr();
            moreWritings();
        } else if (token.getAttr_name() == Attribute.STRING) {
            getToken();
            moreWritings();
        }
    }

    private void moreWritings() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            writing1();
        }
    }

    private void readStatement() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("read")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                reading1();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                    getToken();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
                        getToken();
                    }
                }
            }
        }
    }

    private void reading1() {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attr();
            moreReadings();
        }
    }

    private void moreReadings() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            reading1();
        }
    }

    private void attr() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(".")) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                arrayVerification();
                attr();
            }
        }
    }

    private void whileStatement() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("while")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                expression();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                    getToken();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                        getToken();
                        commands();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                            getToken();
                        }
                    }
                }
            }
        }
    }

    private void ifStatement() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("if")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                expression();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                    getToken();
                    if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("then")) {
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                            getToken();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                                getToken();
                                elseStatement();
                            }
                        }
                    }
                }
            }
        }
    }

    private void elseStatement() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("else")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                getToken();
                commands();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                    getToken();
                }
            }
        }
    }

    private void moreMethods() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            methodDeclaration();
        }
    }

    private void variablesDeclaration() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("variables")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                getToken();
                variable();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                    getToken();
                }
            }
        }
    }

    private void variable() {
        if (verifyType()) {
            getToken();
            variable2();
        }
    }

    private void variable2() {
        name();
        moreVariables();
    }

    private void moreVariables() {
        if (verifyType()) {
            variable();
        }
    }

    private void name() {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            moreNames();
        }
    }

    private void moreNames() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            name();
        }
        else if(token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")){
            getToken();
        }
    }

    private void parameterDeclaration() {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            parameterDeclaration2();
        }
    }

    private void parameterDeclaration2() {
        if (verifyType()|| token.getAttr_name() == Attribute.ID) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                arrayVerification();
                moreParameters();
            }
        }
    }

    private void return1() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("return")) {
            getToken();
            return2();
        }
    }

    private void return2() {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
        } else if(token.getAttr_name() == Attribute.STRING || token.getAttr_name() == Attribute.NUMBER || token.getLexeme().equals("true") || token.getLexeme().equals("false")){
            value();
        }
    }

    private void moreParameters() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            parameterDeclaration2();
        }
    }

    private void arrayVerification() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            getToken();
            arrayIndex();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
                doubleArray();
            }
        }
    }
    
    private void arrayIndex(){
        if(token.getAttr_name() == Attribute.NUMBER || token.getAttr_name() == Attribute.ID){
            getToken();
        }
    }

    private void doubleArray() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            getToken();
            arrayIndex();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
            }
        }
    }

    private void type() {
        if (verifyType()) {
            getToken();
        }
        else if(token.getAttr_name() == Attribute.ID){
            getToken();
        }
    }

    private boolean hasTokens() {
        return this.pos < this.tokens.size();
    }

    public void clear() {
        this.tokens.clear();
        this.pos = 0;
    }

    private boolean verifyType() {
        return token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int") || token.getLexeme().equals("void"));
    }

    private void getToken() {
        System.out.println(token);
        if (hasTokens()) {
            token = tokens.get(pos++);
        }
    }

    private boolean isRelationalOperator() {
        return token.getAttr_name() == Attribute.REL_OP && (token.getLexeme().equals("!=") || token.getLexeme().equals("==")
                || token.getLexeme().equals("<") || token.getLexeme().equals(">") || token.getLexeme().equals("=")
                || token.getLexeme().equals("<=") || token.getLexeme().equals(">="));
    }
}
