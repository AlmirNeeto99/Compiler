package Model;

import Exceptions.*;
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
        try {
            start();
        } catch (EOFException ex) {
            System.out.println(ex.getMessage());;
        }
    }

    public void start() throws EOFException {
        constantDeclaration();
        classDeclaration();
        moreClasses();
        if (this.token == null) {
            System.out.println("Success");
        }
    }

    private void constantDeclaration() throws EOFException {
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

    private void classDeclaration() throws EOFException {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("class")) {
            getToken();
            classIdentification();
        }
    }

    private void moreClasses() throws EOFException {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("class")) {
            classDeclaration();
            moreClasses();
        }
    }

    private void classIdentification() throws EOFException {
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

    private void classHeritage() throws EOFException {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("extends")) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
            }
        }
    }

    private void classBody() throws EOFException {
        classAtributes();
        methods();
    }

    private void classAtributes() throws EOFException {
        variableDeclaration();
    }

    private void variableDeclaration() throws EOFException {
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

    private void constants() throws EOFException {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int") || token.getLexeme().equals("void"))) {
            getToken();
            constantAttribution();
            moreConstants();
        }
    }

    private void moreConstants() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            constantAttribution();
            moreConstants();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
            getToken();
            newDeclaration();
        }
    }

    private void newDeclaration() throws EOFException {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int") || token.getLexeme().equals("void"))) {
            constants();
        }
    }

    private void constantAttribution() throws EOFException {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            if (token.getLexeme().equals("=")) {
                getToken();
                value();
            }
        }
    }

    private void value() throws EOFException {
        if (token.getAttr_name() == Attribute.STRING || token.getAttr_name() == Attribute.NUMBER || token.getLexeme().equals("true") || token.getLexeme().equals("false")) {
            getToken();
        }
    }

    private void methods() throws EOFException {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            methodDeclaration();
        }
    }

    private void methodDeclaration() throws EOFException {
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

    private void commands() throws EOFException {
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

    private void attribution() throws EOFException {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            increment();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                arrayVerification();
                attribute();
            }
        } else if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attribute();
            verif();
        }
    }

    private void verif() throws EOFException {
        if (token.getAttr_name() == Attribute.REL_OP && token.getLexeme().equals("=")) {
            normalAttribution2();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            complement();
        }
    }

    private void complement() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            getToken();
            parameter();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                getToken();
            }
        }
    }

    private void parameter() throws EOFException {
        if (token.getAttr_name() == Attribute.STRING) {
            getToken();
            moreParam();
        } else if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("true") || token.getLexeme().equals("false"))) {
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

    private void parameter2() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            complement();
        }
    }

    private void moreParam() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            obrigatoryParam();
        }
    }

    private void obrigatoryParam() throws EOFException {
        if (token.getAttr_name() == Attribute.STRING) {
            getToken();
            moreParam();
        } else if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("true") || token.getLexeme().equals("false"))) {
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

    private void normalAttribution2() throws EOFException {
        if (token.getAttr_name() == Attribute.REL_OP && token.getLexeme().equals("=")) {
            getToken();
            normalAttribution3();
        } else if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            increment();
        }
    }

    private void normalAttribution3() throws EOFException {
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

    private void expression() throws EOFException {
        addExp();
        relationalExp();
    }

    private void relationalExp() throws EOFException {
        if (isRelationalOperator()) {
            getToken();
            addExp();
            logicalExp();
        } else if (token.getAttr_name() == Attribute.LOGICAL_OP && (token.getLexeme().equals("||") || token.getLexeme().equals("&&"))) {
            logicalExp();
        }
    }

    private void logicalExp() throws EOFException {
        if (token.getAttr_name() == Attribute.LOGICAL_OP && (token.getLexeme().equals("||") || token.getLexeme().equals("&&"))) {
            getToken();
            expression();
        }
    }

    private void addExp() throws EOFException {
        multExp();
        plusOrMinus();
    }

    private void plusOrMinus() throws EOFException {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("+") || token.getLexeme().equals("-"))) {
            getToken();
            addExp();
        }
    }

    private void timesOrDivide() throws EOFException {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("*") || token.getLexeme().equals("/"))) {
            getToken();
            multExp();
        }
    }

    private void multExp() throws EOFException {
        negExp();
        timesOrDivide();
    }

    private void negExp() throws EOFException {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("-") || token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            getToken();
            expValue();
        } else if (token.getAttr_name() == Attribute.LOGICAL_OP && token.getLexeme().equals("!")) {
            getToken();
            expValue();
        } else if (token.getAttr_name() == Attribute.NUMBER || token.getAttr_name() == Attribute.ID) {
            expValue();
            incrementAndDecrement();
        } else if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("true") || token.getLexeme().equals("false"))) {
            expValue();
            incrementAndDecrement();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            expValue();
            incrementAndDecrement();
        }
    }

    private void expValue() throws EOFException {
        if (token.getAttr_name() == Attribute.NUMBER) {
            getToken();
        } else if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attribute();
            parameter2();
        } else if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("true") || token.getLexeme().equals("false"))) {
            getToken();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            getToken();
            expression();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                getToken();
            }
        }
    }

    private void incrementAndDecrement() throws EOFException {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            getToken();
        }
    }

    private void increment() throws EOFException {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            getToken();
        }
    }

    private void writeStatement() throws EOFException {
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

    private void writing1() throws EOFException {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attribute();
            moreWritings();
        } else if (token.getAttr_name() == Attribute.STRING) {
            getToken();
            moreWritings();
        }
    }

    private void moreWritings() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            writing1();
        }
    }

    private void readStatement() throws EOFException {
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

    private void reading1() throws EOFException {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attribute();
            moreReadings();
        }
    }

    private void moreReadings() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            reading1();
        }
    }

    private void attribute() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(".")) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                arrayVerification();
                attribute();
            }
        }
    }

    private void whileStatement() throws EOFException {
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

    private void ifStatement() throws EOFException {
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
                            } else {
                                System.out.println("Expected: \"}\"");
                                System.out.println("Found: " + token.getLexeme());
                            }
                        }
                    } else {
                        System.out.println("Expected: \"then\"");
                        System.out.println("Found: " + token.getLexeme());
                    }
                }
            }
        }
    }

    private void elseStatement() throws EOFException {
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

    private void moreMethods() throws EOFException {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            methodDeclaration();
        }
    }

    private void variablesDeclaration() throws EOFException {
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

    private void variable() throws EOFException {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            getToken();
            name();
            moreVariables();
        }
    }

    private void moreVariables() throws EOFException {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            variable();
        }
    }

    private void name() throws EOFException {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            moreNames();
        }
    }

    private void moreNames() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            name();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
            getToken();
        }
    }

    private void parameterDeclaration() throws EOFException {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            parameterDeclaration2();
        }
    }

    private void parameterDeclaration2() throws EOFException {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                arrayVerification();
                moreParameters();
            }
        }
    }

    private void return1() throws EOFException {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("return")) {
            getToken();
            return2();
        }
    }

    private void return2() throws EOFException {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
        } else if (token.getAttr_name() == Attribute.STRING || token.getAttr_name() == Attribute.NUMBER || token.getLexeme().equals("true") || token.getLexeme().equals("false")) {
            value();
        }
    }

    private void moreParameters() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            parameterDeclaration2();
        }
    }

    private void arrayVerification() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            getToken();
            arrayIndex();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
                doubleArray();
            }
        }
    }

    private void arrayIndex() throws EOFException {
        if (token.getAttr_name() == Attribute.NUMBER || token.getAttr_name() == Attribute.ID) {
            getToken();
        }
    }

    private void doubleArray() throws EOFException {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            getToken();
            arrayIndex();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
            }
            else{
                //throw new UnexpectedTokenException(token, "]", token.getLine());
            }
        }
    }

    private void type() throws EOFException {
        if (verifyType()) {
            getToken();
        } else if (token.getAttr_name() == Attribute.ID) {
            getToken();
        }
    }

    private boolean hasTokens() {
        return this.tokens.size() > 0;
    }

    private boolean verifyType() {
        return token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int") || token.getLexeme().equals("void"));
    }

    private void getToken() throws EOFException {
        System.out.println(token);
        if (hasTokens()) {
            token = tokens.remove(0);
        } else {
            throw new EOFException("Unexpected End of File!");
        }
    }

    private boolean isRelationalOperator() {
        return token.getAttr_name() == Attribute.REL_OP && (token.getLexeme().equals("!=") || token.getLexeme().equals("==")
                || token.getLexeme().equals("<") || token.getLexeme().equals(">") || token.getLexeme().equals("=")
                || token.getLexeme().equals("<=") || token.getLexeme().equals(">="));
    }
}
