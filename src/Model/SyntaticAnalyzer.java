package Model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SyntaticAnalyzer {

    private ArrayList<Token> tokens = new ArrayList<>();
    private Token token;
    private int pos;
    private ArrayList<String> errors = new ArrayList<>();
    private boolean isValid = true;
    private boolean EOF = false;

    public SyntaticAnalyzer() {
        this.pos = 0;
    }

    public void analyze(ArrayList<Token> tokens) throws IOException {
        this.tokens = tokens;
        getToken();
        start();
    }

    public void start() throws IOException {
        constantDeclaration();
        classDeclaration();
        moreClasses();
    }

    /* This method identify a Constant Block*/
    private void constantDeclaration() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("const")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                getToken();
                constants();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                    getToken();
                } else {
                    isValid = false;
                    String er = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(er);
                }
            } else {
                isValid = false;
                String er = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                while (!token.getLexeme().equals("class") && !EOF) {
                    getToken();
                }
            }
        }
    }

    private void classDeclaration()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("class")) {
            getToken();
            classIdentification();
        }
    }

    private void moreClasses()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("class")) {
            classDeclaration();
            moreClasses();
        }
    }

    /* This method identify a class*/
    private void classIdentification() {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            classHeritage();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                getToken();
                classBody();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                    getToken();
                } else {
                    isValid = false;
                    String er = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(er);
                }
            } else { //If the block wasn't open goes to next class (if exists)
                isValid = false;
                String er = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                while (!token.getLexeme().equals("class") && !EOF) {
                    getToken();
                }
            }
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            classHeritage();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                getToken();
                classBody();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                    getToken();
                } else {
                    isValid = false;
                    String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(err);
                }
            } else { //If the block wasn't open goes to next class (if exists)
                isValid = false;
                String er2 = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er2);
                while (!token.getLexeme().equals("class") && !EOF) {
                    getToken();
                }
            }
        }
    }

    /*This method check if the heritage of a class is correct*/
    private void classHeritage() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("extends")) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
            } else {
                isValid = false;
                String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void classBody()  {
        classAtributes();
        methods();
    }

    private void classAtributes()  {
        variableDeclaration();
    }

    private void variableDeclaration()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("variables")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                getToken();
                variable();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                    getToken();
                } else {
                    isValid = false;
                    String er = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(er);
                }
            } else {
                isValid = false;
                String er = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                while ((!token.getLexeme().equals("method") || !token.getLexeme().equals("class")) && !EOF) {
                    getToken();
                }
            }
        }
    }

    /*Verify if at least one constant was declared */
    private void constants() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int"))) {
            getToken();
            constantAttribution();
            moreConstants();
        } else { //If no constant was declared it go to the next class.
            isValid = false;
            String er = "Expected Token: 'float | int | string | bool' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals("}") && !EOF) {
                getToken();
            }
        }
    }

    /*Verify if there are more constants in one line or if it's over.*/
    private void moreConstants() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            constantAttribution();
            moreConstants();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
            getToken();
            newDeclaration();
        } else {
            isValid = false;
            String er = "Expected Token: '; | ,' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals("}") && !EOF) {
                getToken();
            }
        }
    }

    private void newDeclaration() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int"))) {
            constants();
        }
    }

    /*Verify if the attribution is correct*/
    private void constantAttribution() {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            if (token.getLexeme().equals("=")) {
                getToken();
                value();
            } else {
                isValid = false;
                String er = "Expected Token: '=' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                while (!token.getLexeme().equals(";") && !EOF) {
                    getToken();
                }
            }
        }
    }

    private void value()  {
        if (token.getAttr_name() == Attribute.STRING || token.getAttr_name() == Attribute.NUMBER || token.getLexeme().equals("true") || token.getLexeme().equals("false")) {
            getToken();
        } else {
            isValid = false;
            String er = "Expected Token: 'String or Number or 'true' or 'false'' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals(";") && !EOF) {
                getToken();
            }
        }
    }

    private void methods()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            methodDeclaration();
        }
    }

    private void methodDeclaration()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            getToken();
            type();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) { //OKAY
                    getToken();
                    parameterDeclaration();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) { //OKAY
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                            getToken();
                            variableDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                moreMethods();
                            } else {
                                isValid = false;
                                String er = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(er);
                                moreMethods();
                            }
                        } else {
                            isValid = false;
                            String er = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(er);
                            variableDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                moreMethods();
                            } else {
                                isValid = false;
                                String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err);
                                moreMethods();
                            }
                        }
                    } else {
                        isValid = false;
                        String er = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(er);
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                            getToken();
                            variableDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                moreMethods();
                            } else {
                                isValid = false;
                                String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err);
                                moreMethods();
                            }
                        } else {
                            isValid = false;
                            String err = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err);
                            variableDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                moreMethods();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                                moreMethods();
                            }
                        }
                    }
                } else {
                    isValid = false;
                    String er = "Expected Token: '(' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(er);
                    parameterDeclaration();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) { //OKAY
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                            getToken();
                            variableDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                moreMethods();
                            } else {
                                isValid = false;
                                String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err);
                                moreMethods();
                            }
                        } else {
                            isValid = false;
                            String err = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err);
                            variableDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                moreMethods();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                                moreMethods();
                            }
                        }
                    } else {
                        isValid = false;
                        String err = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err);
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                            getToken();
                            variableDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                moreMethods();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                                moreMethods();
                            }
                        } else {
                            isValid = false;
                            String err1 = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err1);
                            variableDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                moreMethods();
                            } else {
                                isValid = false;
                                String err2 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err2);
                                moreMethods();
                            }
                        }
                    }
                }
            } else {
                isValid = false;
                String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                    getToken();
                    parameterDeclaration();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                            getToken();
                            variableDeclaration();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                                getToken();
                                moreMethods();
                            } else {
                                isValid = false;
                                String er3 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(er3);
                            }
                        } else {
                            isValid = false;
                            String er2 = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(er2);
                            while ((!token.getLexeme().equals("method") && !token.getLexeme().equals("class")) && !EOF) {
                                getToken();
                            }
                        }
                    } else {
                        isValid = false;
                        String er1 = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(er1);
                        while ((!token.getLexeme().equals("method") && !token.getLexeme().equals("class")) && !EOF) {
                            getToken();
                        }
                    }
                } else {
                    isValid = false;
                    String err = "Expected Token: '(' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(err);
                    while ((!token.getLexeme().equals("method") && !token.getLexeme().equals("class")) && !EOF) {
                        getToken();
                    }
                }
            }
        }
    }

    private void commands()  {
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
            } else {
                isValid = false;
                String er = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        } else if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            attribution();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
                getToken();
            } else {
                isValid = false;
                String er = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
            commands();
        } else if (token.getAttr_name() == Attribute.ID) {
            attribution();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
                getToken();
            } else {
                isValid = false;
                String er = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
            commands();
        }
    }

    private void attribution()  {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            increment();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                arrayVerification();
                attribute();
            } else {
                isValid = false;
                String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                while (!token.getLexeme().equals(";") && !EOF) {
                    getToken();
                }
            }
        } else if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attribute();
            verif();
        }
    }

    private void verif()  {
        if (token.getAttr_name() == Attribute.REL_OP && token.getLexeme().equals("=")) {
            normalAttribution2();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            complement();
        } else {
            isValid = false;
            String er = "Expected Token: '= or (' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals(";") && !EOF) {
                getToken();
            }
        }
    }

    private void complement()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            getToken();
            parameter();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                getToken();
            } else {
                isValid = false;
                String er = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void parameter()  {
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

    private void parameter2()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            complement();
        }
    }

    private void moreParam()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            obrigatoryParam();
        }
    }

    private void obrigatoryParam()  {
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

    private void normalAttribution2()  {
        if (token.getAttr_name() == Attribute.REL_OP && token.getLexeme().equals("=")) {
            getToken();
            normalAttribution3();
        } else if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            increment();
        }
    }

    private void normalAttribution3()  {
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
        } else {
            isValid = false;
            String er = "Expected Token: 'String | 'true' | 'false' | '!' | Number | IDENTIFIER | '(' | '++' | '--' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
        }
    }

    private void expression()  {
        addExp();
        relationalExp();
    }

    private void relationalExp()  {
        if (isRelationalOperator()) {
            getToken();
            addExp();
            logicalExp();
        } else if (token.getAttr_name() == Attribute.LOGICAL_OP && (token.getLexeme().equals("||") || token.getLexeme().equals("&&"))) {
            logicalExp();
        }
    }

    private void logicalExp()  {
        if (token.getAttr_name() == Attribute.LOGICAL_OP && (token.getLexeme().equals("||") || token.getLexeme().equals("&&"))) {
            getToken();
            expression();
        }
    }

    private void addExp()  {
        multExp();
        plusOrMinus();
    }

    private void plusOrMinus()  {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("+") || token.getLexeme().equals("-"))) {
            getToken();
            addExp();
        }
    }

    private void timesOrDivide()  {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("*") || token.getLexeme().equals("/"))) {
            getToken();
            multExp();
        }
    }

    private void multExp()  {
        negExp();
        timesOrDivide();
    }

    private void negExp()  {
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
        } else {
            isValid = false;
            String er = "Expected Token: 'String | 'true' | 'false' | '!' | Number | IDENTIFIER | '(' | '++' | '--' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
        }
    }

    private void expValue()  {
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
        } else {
            isValid = false;
            String er = "Expected Token: ''true' | 'false' | Number | IDENTIFIER | '(' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
        }
    }

    private void incrementAndDecrement()  {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            getToken();
        }
    }

    private void increment()  {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            getToken();
        }
    }

    private void writeStatement()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("write")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                writing1();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) { //OKAY
                    getToken();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) { //OKAY
                        getToken();
                    } else {
                        isValid = false;
                        String er = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(er);
                    }
                } else {
                    isValid = false;
                    String er = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(er);
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) { //OKAY
                        getToken();
                    } else {
                        isValid = false;
                        String err = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err);
                    }
                }
            } else {
                isValid = false;
                String er = "Expected Token: '(' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                writing1();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) { //OKAY
                    getToken();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) { //OKAY
                        getToken();
                    } else {
                        isValid = false;
                        String err = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err);
                    }
                } else {
                    isValid = false;
                    String err = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(err);
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) { //OKAY
                        getToken();
                    } else {
                        isValid = false;
                        String err1 = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err1);
                    }
                }
            }
        }
    }

    private void writing1()  {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attribute();
            moreWritings();
        } else if (token.getAttr_name() == Attribute.STRING) {
            getToken();
            moreWritings();
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER or String' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
        }
    }

    private void moreWritings()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            writing1();
        }
    }

    private void readStatement()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("read")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                reading1();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) { //OKAY
                    getToken();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) { //OKAY
                        getToken();
                    } else {
                        isValid = false;
                        String er = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(er);
                    }
                } else {
                    isValid = false;
                    String er = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(er);
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) { //OKAY
                        getToken();
                    } else {
                        isValid = false;
                        String err = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err);
                    }
                }
            } else {
                isValid = false;
                String er = "Expected Token: '(' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                reading1();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) { //OKAY
                    getToken();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) { //OKAY
                        getToken();
                    } else {
                        isValid = false;
                        String err = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err);
                    }
                } else {
                    isValid = false;
                    String err = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(err);
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) { //OKAY
                        getToken();
                    } else {
                        isValid = false;
                        String err1 = "Expected Token: ';' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err1);
                    }
                }
            }
        }
    }

    private void reading1()  {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            attribute();
            moreReadings();
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
        }
    }

    private void moreReadings()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            reading1();
        }
    }

    private void attribute()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(".")) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                arrayVerification();
                attribute();
            } else {
                isValid = false;
                String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void whileStatement()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("while")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                expression();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                    getToken();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //Okay
                        getToken();
                        commands();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //Okay
                            getToken();
                            elseStatement();
                        } else {
                            isValid = false;
                            String er = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(er);
                            elseStatement();
                        }
                    } else {
                        isValid = false;
                        String er = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(er);
                        commands();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                            getToken();
                        } else {
                            isValid = false;
                            String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err);
                        }
                    }

                } else {
                    isValid = false;
                    String er = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(er);
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //Okay
                        getToken();
                        commands();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //Okay
                            getToken();
                            elseStatement();
                        } else {
                            isValid = false;
                            String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err);
                            elseStatement();
                        }
                    } else {
                        isValid = false;
                        String err = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err);
                        commands();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                            getToken();
                        } else {
                            isValid = false;
                            String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err1);
                        }
                    }

                }
            } else {
                isValid = false;
                String er = "Expected Token: '(' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                expression();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                    getToken();
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                        getToken();
                        commands();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                            getToken();
                            elseStatement();
                        } else {
                            isValid = false;
                            String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err);
                            elseStatement();
                        }
                    } else {
                        isValid = false;
                        String err = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err);
                        commands();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                            getToken();
                            elseStatement();
                        } else {
                            isValid = false;
                            String err3 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err3);
                            elseStatement();
                        }
                    }

                } else {
                    isValid = false;
                    String err = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(err);
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //Okay
                        getToken();
                        commands();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //Okay
                            getToken();
                        } else {
                            isValid = false;
                            String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err1);
                        }
                    } else {
                        isValid = false;
                        String err2 = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err2);
                        commands();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                            getToken();
                        } else {
                            isValid = false;
                            String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err1);
                        }
                    }

                }
            }
        }
    }

    private void ifStatement()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("if")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                expression();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                    getToken();
                    if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("then")) {
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //Okay
                            getToken();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //Okay
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String er = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(er);
                                elseStatement();
                            }
                        } else {
                            isValid = false;
                            String er = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(er);
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err);
                                elseStatement();
                            }
                        }
                    } else {
                        isValid = false;
                        String er = "Expected Token: 'then' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(er);
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                            getToken();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err);
                                elseStatement();
                            }
                        } else {
                            isValid = false;
                            String err = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err);
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                                elseStatement();
                            }
                        }
                    }
                } else {
                    isValid = false;
                    String er = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(er);
                    if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("then")) {
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //Okay
                            getToken();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //Okay
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err);
                                elseStatement();
                            }
                        } else {
                            isValid = false;
                            String err = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err);
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                                elseStatement();
                            }
                        }
                    } else {
                        isValid = false;
                        String err = "Expected Token: 'then' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err);
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                            getToken();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                                elseStatement();
                            }
                        } else {
                            isValid = false;
                            String err2 = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err2);
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                                elseStatement();
                            }
                        }
                    }
                }
            } else {
                isValid = false;
                String er = "Expected Token: '(' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                expression();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) {
                    getToken();
                    if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("then")) { //OKAY
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                            getToken();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err);
                                elseStatement();
                            }
                        } else {
                            isValid = false;
                            String err = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err);
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err3 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err3);
                                elseStatement();
                            }
                        }
                    } else {
                        isValid = false;
                        String err = "Expected Token: 'then' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err);
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                            getToken();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String er3 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(er3);
                            }
                        } else {
                            isValid = false;
                            String er2 = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(er2);
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                            }
                        }
                    }
                } else {
                    isValid = false;
                    String err = "Expected Token: ')' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(err);
                    if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("then")) {
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //Okay
                            getToken();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //Okay
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                                elseStatement();
                            }
                        } else {
                            isValid = false;
                            String err2 = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err2);
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) {
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err1 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err1);
                                elseStatement();
                            }
                        }
                    } else {
                        isValid = false;
                        String err1 = "Expected Token: 'then' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                        errors.add(err1);
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                            getToken();
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err2 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err2);
                                elseStatement();
                            }
                        } else {
                            isValid = false;
                            String err2 = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                            errors.add(err2);
                            commands();
                            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                                getToken();
                                elseStatement();
                            } else {
                                isValid = false;
                                String err3 = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                                errors.add(err3);
                                elseStatement();
                            }
                        }
                    }
                }
            }
        }
    }

    private void elseStatement()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("else")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) {
                getToken();
                commands();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                    getToken();
                } else {
                    isValid = false;
                    String er = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(er);
                }
            } else {
                isValid = false;
                String er = "Expected Token: '{' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                commands();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                    getToken();
                } else {
                    isValid = false;
                    String err = "Expected Token: '}' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                    errors.add(err);
                }
            }
        }
    }

    private void moreMethods()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            methodDeclaration();
        }
    }

    private void variable() {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            getToken();
            name();
            moreVariables();
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER | int | string | bool | void | float' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while ((!token.getLexeme().equals("method") && !token.getLexeme().equals("class") && !token.getLexeme().equals("}")) && !EOF) {
                getToken();
            }
        }
    }

    private void moreVariables()  {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            variable();
        }
    }

    private void name()  {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
            moreNames();
        } else {
            isValid = false;
            String er = "Expected Token: IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals(";") && !EOF) {
                getToken();
            }
            moreNames();
        }
    }

    private void moreNames()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            name();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(";")) {
            getToken();
        } else {
            isValid = false;
            String er = "Expected Token: ', or ;' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals(";") && !EOF) {
                getToken();
            }
        }
    }

    private void parameterDeclaration()  {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            parameterDeclaration2();
        }
    }

    private void parameterDeclaration2()  {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                getToken();
                arrayVerification();
                moreParameters();
            } else {
                isValid = false;
                String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                while (!token.getLexeme().equals(")") && !EOF) {
                    getToken();
                }
            }
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
        }
    }

    private void return1()  {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("return")) {
            getToken();
            return2();
        }
    }

    private void return2()  {
        if (token.getAttr_name() == Attribute.ID) {
            getToken();
            arrayVerification();
        } else if (token.getAttr_name() == Attribute.STRING || token.getAttr_name() == Attribute.NUMBER || token.getLexeme().equals("true") || token.getLexeme().equals("false")) {
            value();
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER or String or 'true' or 'false'' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals(";") && !EOF) {
                getToken();
            }
        }
    }

    private void moreParameters()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            parameterDeclaration2();
        }
    }

    private void arrayVerification()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            getToken();
            arrayIndex();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
                doubleArray();
            } else {
                isValid = false;
                String er = "Expected Token: ']' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void arrayIndex()  {
        if (token.getAttr_name() == Attribute.NUMBER || token.getAttr_name() == Attribute.ID) {
            getToken();
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER or NUMBER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals("]") && !EOF) {
                getToken();
            }
        }
    }

    private void doubleArray()  {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            getToken();
            arrayIndex();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
            } else {
                isValid = false;
                String er = "Expected Token: ']' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void type()  {
        if (verifyType()) {
            getToken();
        } else if (token.getAttr_name() == Attribute.ID) {
            getToken();
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER | string | float | bool | int | void ' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
        }
    }

    private boolean hasTokens() {
        return this.pos < this.tokens.size();
    }

    public void clear() {
        this.tokens.clear();
        this.pos = 0;
        this.EOF = false;
        this.isValid = true;
        this.errors.clear();
        this.token = null;
    }

    private boolean verifyType() {
        return token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int") || token.getLexeme().equals("void"));
    }

    private void getToken() {
        if (hasTokens()) {
            token = tokens.get(pos++);
        } else {
            EOF = true;
        }
    }

    private boolean isRelationalOperator() {
        return token.getAttr_name() == Attribute.REL_OP && (token.getLexeme().equals("!=") || token.getLexeme().equals("==")
                || token.getLexeme().equals("<") || token.getLexeme().equals(">") || token.getLexeme().equals("=")
                || token.getLexeme().equals("<=") || token.getLexeme().equals(">="));
    }

    public void writeOutput(File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("output_"+file.getName())))) {
            if (isValid && EOF) {
                bw.write("-> Success on Parsing File!!!");
            } else {
                if(!EOF){
                    errors.add("-> Unexpected End of File!!!\n-> There are unalized tokens!\n-> Check if are exceeded '}'\n");
                }
                bw.write("-> Error on Parsing File!!!\n");
                for (String s : errors) {
                    bw.write(s + "\n");
                }
            }
        }
    }
}
