package Model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class SyntaticAnalyzer {

    private ArrayList<Token> tokens = new ArrayList<>();

    private Token token;
    private int pos;
    private ArrayList<String> errors = new ArrayList<>();

    private boolean isValid = true;
    private boolean EOF = false;
    private ArrayList<ClassTable> table = new ArrayList();
    private ArrayList<String[]> constants = new ArrayList();

    private ArrayList<Token> expression = new ArrayList();

    private boolean inConst = false;
    private boolean inMethod = false;
    private boolean inVariable = false;
    private boolean hasMain = false;

    private ClassTable actualClass;
    private VariableTable actualVariable;
    private MethodTable actualMethod;

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
            inConst = true;
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                getToken();
                constants();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("}")) { //OKAY
                    getToken();
                } else {
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
            verifyConstantsTypes();
        }
        inConst = false;
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

    /* This method identify a class*/
    private void classIdentification() {
        if (token.getAttr_name() == Attribute.ID) {
            ClassTable classe = new ClassTable(token.getLexeme());
            if (table.contains(classe)) {
                errors.add("There's already a class named '" + token.getLexeme() + "' at Line: " + token.getLine());
                isValid = false;
            } else {
                table.add(classe);
                actualClass = table.get(table.size() - 1);
            }
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
                if (actualClass.getName().equals(token.getLexeme())) {
                    isValid = false;
                    errors.add("A class can't inherit from itself, at Line: " + token.getLine());
                } else {
                    ClassTable heritage = new ClassTable(token.getLexeme());
                    if (table.contains(heritage)) {
                        actualClass.setHeritage(token.getLexeme());
                        for (ClassTable c : table) {
                            if (c.getName().equals(token.getLexeme())) {
                                ArrayList<String[]> s = c.getVariables().getVariables();
                                for (String[] a : s) {
                                    actualClass.getVariables().getVariables().add(a);
                                }
                                ArrayList<MethodTable> m = c.getMethods();
                                for (MethodTable mt : m) {
                                    actualClass.getMethods().add(mt);
                                }
                            }
                        }
                    } else {
                        errors.add("There's no class named '" + token.getLexeme() + "' to be inherited, at Line: " + token.getLine());
                        isValid = false;
                    }
                }
                getToken();
            } else {
                isValid = false;
                String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void classBody() {
        classAtributes();
        methods();
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
        inVariable = false;
    }

    /*Verify if at least one constant was declared */
    private void constants() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float") || token.getLexeme().equals("string") || token.getLexeme().equals("bool") || token.getLexeme().equals("int"))) {
            String[] attr = new String[3];
            attr[0] = token.getLexeme();
            constants.add(attr);
            getToken();
            constantAttribution(attr);
            moreConstants(attr);
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
    private void moreConstants(String[] attr) {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            String[] attr2 = new String[3];
            attr2[0] = attr[0];
            constants.add(attr2);
            getToken();
            constantAttribution(attr2);
            moreConstants(attr2);
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
    private void constantAttribution(String[] attr) {
        if (token.getAttr_name() == Attribute.ID) {
            for (String[] s : constants) {
                if (s[1] != null) {
                    if (s[1].equals(token.getLexeme())) {
                        isValid = false;
                        errors.add("There's already a constant named: '" + token.getLexeme() + "' at Line: " + token.getLine());
                    }
                }
            }
            attr[1] = token.getLexeme();
            getToken();
            if (token.getLexeme().equals("=")) {
                getToken();
                value(attr);
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

    private void value(String attr[]) {
        if (token.getAttr_name() == Attribute.STRING || token.getAttr_name() == Attribute.NUMBER || token.getLexeme().equals("true") || token.getLexeme().equals("false")) {
            if (inConst) {
                attr[2] = token.getLexeme();
            }
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

    private void methods() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            methodDeclaration();
        }
    }

    private void methodDeclaration() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            inMethod = true;
            getToken();
            String type = type();
            if (!type.equals("void") && !type.equals("int") && !type.equals("float") && !type.equals("bool") && !type.equals("string")) {
                ClassTable c = new ClassTable(type);
                if (!table.contains(c)) {
                    isValid = false;
                    errors.add("There's no class named: '" + type + "' to be returned by the method.");
                }
            }
            if (token.getAttr_name() == Attribute.ID || token.getLexeme().equals("main")) {
                if (token.getLexeme().equals("main")) {
                    if (hasMain) {
                        isValid = false;
                        errors.add("There can exist only one 'main' method, at Line: " + token.getLine());
                    }
                    hasMain = true;
                }
                MethodTable m = new MethodTable();
                m.setName(token.getLexeme());
                m.setType(type);
                actualMethod = m;
                getToken();
                if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) { //OKAY
                    getToken();
                    parameterDeclaration(m);
                    boolean equals = false;
                    for (MethodTable mt : actualClass.getMethods()) {
                        if (mt.getParameters().size() == m.getParameters().size()) {
                            ArrayList<String[]> a = mt.getParameters();
                            ArrayList<String[]> b = m.getParameters();
                            for (int control = 0; control < a.size(); control++) {
                                String[] mtParam = a.get(control);
                                String[] mParam = b.get(control);
                                if (mtParam[0].equals(mParam[0]) && mtParam[2].equals(mParam[2])) {
                                    equals = true;
                                } else {
                                    equals = false;
                                }
                            }
                        }
                    }
                    if (!equals) {
                        actualClass.getMethods().add(m);
                    } else {
                        isValid = false;
                        errors.add("There's already a method named: '" + m.getName() + "' with the same parameters, at Class: '" + actualClass.getName() + "'");
                    }
                    if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(")")) { //OKAY
                        getToken();
                        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("{")) { //OKAY
                            getToken();
                            variableDeclaration();
                            commands();
                            if (!actualMethod.getType().equals("void") && actualMethod.getReturns().isEmpty()) {
                                isValid = false;
                                errors.add("Method '" + actualMethod.getName() + "' has no valid return command, at class: '" + actualClass.getName() + "'.");
                            }
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
                    parameterDeclaration(new MethodTable());
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
                    parameterDeclaration(new MethodTable());
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
        inMethod = false;
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

    private void attribution() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            increment();
            expression.clear();
            ArrayList<String> attr = new ArrayList();
            if (token.getAttr_name() == Attribute.ID) {
                attr.add(token.getLexeme());
                String[] a = new String[3];
                a[1] = token.getLexeme();
                a[2] = "null";
                getToken();
                arrayVerification(a);
                boolean matches = false;
                for (String[] s : actualMethod.getVariables().getVariables()) {
                    if (s[1].equals(a[1])) {
                        matches = true;
                        if (s[2].equals(a[2])) {
                        } else {
                            matches = true;
                            isValid = false;
                            if (s[2].equals("array")) {
                                errors.add("Variable '" + a[1] + "' at method Variables, is an array. Line: " + token.getLine());
                            } else if (s[2].equals("matrix")) {
                                errors.add("Variable '" + a[1] + "' at method Variables, is a matrix. Line: " + token.getLine());
                            } else {
                                errors.add("Variable '" + a[1] + "' at method Variables, is neither an array nor a matrix. Line: " + token.getLine());
                            }
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : actualMethod.getParameters()) {
                        if (s[1].equals(a[1])) {
                            if (s[2].equals(a[2])) {
                                matches = true;
                            } else {
                                matches = true;
                                isValid = false;
                                if (s[2].equals("array")) {
                                    errors.add("Variable '" + a[1] + "' at method parameters, is an array. Line: " + token.getLine());
                                } else if (s[2].equals("matrix")) {
                                    errors.add("Variable '" + a[1] + "' at method parameters, is a matrix. Line: " + token.getLine());
                                } else {
                                    errors.add("Variable '" + a[1] + "' at method parameters, is neither an array nor a matrix. Line: " + token.getLine());
                                }
                            }
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : actualClass.getVariables().getVariables()) {
                        if (s[1].equals(a[1])) {
                            if (s[2].equals(a[2])) {
                                matches = true;
                            } else {
                                matches = true;
                                isValid = false;
                                if (s[2].equals("array")) {
                                    errors.add("Variable '" + a[1] + "' at class variables, is an array. Line: " + token.getLine());
                                } else if (s[2].equals("matrix")) {
                                    errors.add("Variable '" + a[1] + "' at class variables, is a matrix. Line: " + token.getLine());
                                } else {
                                    errors.add("Variable '" + a[1] + "' at class variables, is neither an array nor a matrix. Line: " + token.getLine());
                                }
                            }
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : constants) {
                        if (s[1].equals(a[1])) {
                            matches = true;
                            isValid = false;
                            errors.add("You can't increment or decrement a constant");
                        }
                    }
                }
                if (!matches) {
                    isValid = false;
                    errors.add("There's no variables called: '" + a[1] + "', at Line:" + token.getLine());
                }
                attribute(attr);
                boolean object = false;
                if (attr.size() > 0) {
                    String s = attr.get(attr.size() - 1);
                }
            } else {
                isValid = false;
                String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
                while (!token.getLexeme().equals(";") && !EOF) {
                    getToken();
                }
            }
        } else if (token.getAttr_name() == Attribute.ID) {
            ArrayList<String> attr = new ArrayList();
            attr.add(token.getLexeme());
            String[] a = new String[3];
            a[1] = token.getLexeme();
            a[2] = "null";
            getToken();
            arrayVerification(a);
            boolean matches = false;
            for (String[] s : actualMethod.getVariables().getVariables()) {
                if (s[1].equals(a[1])) {
                    matches = true;
                    if (s[2].equals(a[2])) {
                    } else {
                        matches = true;
                        isValid = false;
                        if (s[2].equals("array")) {
                            errors.add("Variable '" + a[1] + "' at method Variables, is an array. Line: " + token.getLine());
                        } else if (s[2].equals("matrix")) {
                            errors.add("Variable '" + a[1] + "' at method Variables, is a matrix. Line: " + token.getLine());
                        } else {
                            errors.add("Variable '" + a[1] + "' at method Variables, is neither an array nor a matrix. Line: " + token.getLine());
                        }
                    }
                }
            }
            if (!matches) {
                for (String[] s : actualMethod.getParameters()) {
                    if (s[1].equals(a[1])) {
                        if (s[2].equals(a[2])) {
                            matches = true;
                        } else {
                            matches = true;
                            isValid = false;
                            if (s[2].equals("array")) {
                                errors.add("Variable '" + a[1] + "' at method parameters, is an array. Line: " + token.getLine());
                            } else if (s[2].equals("matrix")) {
                                errors.add("Variable '" + a[1] + "' at method parameters, is a matrix. Line: " + token.getLine());
                            } else {
                                errors.add("Variable '" + a[1] + "' at method parameters, is neither an array nor a matrix. Line: " + token.getLine());
                            }
                        }
                    }
                }
            }
            if (!matches) {
                for (String[] s : actualClass.getVariables().getVariables()) {
                    if (s[1].equals(a[1])) {
                        if (s[2].equals(a[2])) {
                            matches = true;
                        } else {
                            matches = true;
                            isValid = false;
                            if (s[2].equals("array")) {
                                errors.add("Variable '" + a[1] + "' at class variables, is an array. Line: " + token.getLine());
                            } else if (s[2].equals("matrix")) {
                                errors.add("Variable '" + a[1] + "' at class variables, is a matrix. Line: " + token.getLine());
                            } else {
                                errors.add("Variable '" + a[1] + "' at class variables, is neither an array nor a matrix. Line: " + token.getLine());
                            }
                        }
                    }
                }
            }
            attribute(attr);
            verif();
        }
    }

    private void verif() {
        if ((token.getAttr_name() == Attribute.REL_OP && token.getLexeme().equals("=")) || (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--")))) {
            normalAttribution2();
        } else if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
            complement();
        } else {
            isValid = false;
            String er = "Expected Token: '= or ( or ++ or --' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals(";") && !EOF) {
                getToken();
            }
        }
    }

    private void complement() {
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

    private void parameter() {
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

    private void parameter2() {
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
        } else {
            isValid = false;
            String er = "Expected Token: 'String | 'true' | 'false' | '!' | Number | IDENTIFIER | '(' | '++' | '--' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
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
        plusOrMinus();
    }

    private void plusOrMinus() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("+") || token.getLexeme().equals("-"))) {
            expression.add(token);
            getToken();
            addExp();
        }
    }

    private void timesOrDivide() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("*") || token.getLexeme().equals("/"))) {
            expression.add(token);
            getToken();
            multExp();
        }
    }

    private void multExp() {
        negExp();
        timesOrDivide();
    }

    private void negExp() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("-") || token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            expression.add(token);
            getToken();
            expValue();
        } else if (token.getAttr_name() == Attribute.LOGICAL_OP && token.getLexeme().equals("!")) {
            expression.add(token);
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

    private void expValue() {
        expression.add(token);
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

    private void incrementAndDecrement() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            expression.add(token);
            getToken();
        }
    }

    private void increment() {
        if (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--"))) {
            expression.add(token);
            getToken();
        }
    }

    private void writeStatement() {
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

    private void writing1() {
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

    private void reading1() {
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

    private void moreReadings() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            reading1();
        }
    }

    private void attribute() {
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

    private void attribute(ArrayList<String> attr) {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(".")) {
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                attr.add(token.getLexeme());
                String[] a = new String[3];
                a[1] = token.getLexeme();
                a[2] = "null";
                getToken();
                arrayVerification(a);
                boolean matches = false;
                for (String[] s : actualMethod.getVariables().getVariables()) {
                    if (s[1].equals(a[1])) {
                        matches = true;
                        if (s[2].equals(a[2])) {
                        } else {
                            matches = true;
                            isValid = false;
                            if (s[2].equals("array")) {
                                errors.add("Variable '" + a[1] + "' at method Variables, is an array. Line: " + token.getLine());
                            } else if (s[2].equals("matrix")) {
                                errors.add("Variable '" + a[1] + "' at method Variables, is a matrix. Line: " + token.getLine());
                            } else {
                                errors.add("Variable '" + a[1] + "' at method Variables, is neither an array nor a matrix. Line: " + token.getLine());
                            }
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : actualMethod.getParameters()) {
                        if (s[1].equals(a[1])) {
                            if (s[2].equals(a[2])) {
                                matches = true;
                            } else {
                                matches = true;
                                isValid = false;
                                if (s[2].equals("array")) {
                                    errors.add("Variable '" + a[1] + "' at method parameters, is an array. Line: " + token.getLine());
                                } else if (s[2].equals("matrix")) {
                                    errors.add("Variable '" + a[1] + "' at method parameters, is a matrix. Line: " + token.getLine());
                                } else {
                                    errors.add("Variable '" + a[1] + "' at method parameters, is neither an array nor a matrix. Line: " + token.getLine());
                                }
                            }
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : actualClass.getVariables().getVariables()) {
                        if (s[1].equals(a[1])) {
                            if (s[2].equals(a[2])) {
                                matches = true;
                            } else {
                                matches = true;
                                isValid = false;
                                if (s[2].equals("array")) {
                                    errors.add("Variable '" + a[1] + "' at class variables, is an array. Line: " + token.getLine());
                                } else if (s[2].equals("matrix")) {
                                    errors.add("Variable '" + a[1] + "' at class variables, is a matrix. Line: " + token.getLine());
                                } else {
                                    errors.add("Variable '" + a[1] + "' at class variables, is neither an array nor a matrix. Line: " + token.getLine());
                                }
                            }
                        }
                    }
                }
                attribute(attr);
            } else {
                isValid = false;
                String er = "Expected Token: 'IDENTIFIER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void whileStatement() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("while")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                expression();
                checkIfExpressionIsBoolean();
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

    private void ifStatement() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("if")) {
            getToken();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("(")) {
                getToken();
                expression();
                checkIfExpressionIsBoolean();
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

    private void elseStatement() {
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

    private void moreMethods() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("method")) {
            methodDeclaration();
        }
    }

    private void variable() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && (token.getLexeme().equals("float")
                || token.getLexeme().equals("int") || token.getLexeme().equals("bool")
                || token.getLexeme().equals("string"))
                || token.getAttr_name() == Attribute.ID) {
            inVariable = true;
            if (inMethod) {
                MethodTable m = actualClass.getMethods().get(actualClass.getMethods().size() - 1);
                actualVariable = m.getVariables();
                if (token.getAttr_name() == Attribute.ID) {
                    boolean exists = false;
                    for (ClassTable c : table) {
                        if (c.getName().equals(token.getLexeme())) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        isValid = false;
                        errors.add("There's no class named '" + token.getLexeme() + "' at Line: " + token.getLine() + ", to create an object.");
                    }
                }
            } else {
                actualVariable = table.get(table.size() - 1).getVariables();
                if (token.getAttr_name() == Attribute.ID) {
                    boolean exists = false;
                    for (ClassTable c : table) {
                        if (c.getName().equals(token.getLexeme())) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        isValid = false;
                        errors.add("There's no class named '" + token.getLexeme() + "' at Line: " + token.getLine() + ", to create an object.");
                    }
                }
            }
            String[] attr = new String[3];
            attr[0] = token.getLexeme();
            attr[2] = "null";
            actualVariable.getVariables().add(attr);
            getToken();
            name();
            moreVariables();
            inVariable = false;
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER | int | string | bool | float' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while ((!token.getLexeme().equals("method") && !token.getLexeme().equals("class") && !token.getLexeme().equals("}")) && !EOF) {
                getToken();
            }
        }
    }

    private void moreVariables() {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            variable();
        }
    }

    private void name() {
        if (token.getAttr_name() == Attribute.ID) {
            if (inMethod) {
                MethodTable m = actualClass.getMethods().get(actualClass.getMethods().size() - 1);
                actualVariable = m.getVariables();
                boolean error = false;
                String var = token.getLexeme();
                for (String[] s : actualVariable.getVariables()) {
                    if (s[1] != null) {
                        if (s[1].equals(var)) {
                            error = true;
                            isValid = false;
                            errors.add("There's already a variable called '" + var + "' at Line: " + token.getLine());
                        }
                    }
                }
                if (!error) {
                    for (String[] s : actualMethod.getParameters()) {
                        if (s[1].equals(var)) {
                            error = true;
                            isValid = false;
                            errors.add("There's already a variable called '" + var + "' at method's: '" + actualMethod.getName() + "' parameters.");
                        }
                    }
                }
                if (!error) {
                    for (String[] s : constants) {
                        if (s[1].equals(var)) {
                            error = true;
                            isValid = false;
                            errors.add("There's already a constant called '" + var + "' at method's: '" + actualMethod.getName() + "' variables.");
                        }
                    }
                }
            } else {
                boolean error = false;
                actualVariable = table.get(table.size() - 1).getVariables();
                String var = token.getLexeme();
                for (String[] s : actualVariable.getVariables()) {
                    if (s[1] != null) {
                        if (s[1].equals(var)) {
                            error = true;
                            isValid = false;
                            errors.add("There's already a variable called '" + var + "' at Line: " + token.getLine());
                        }
                    }
                }
                if (!error) {
                    for (String[] s : constants) {
                        if (s[1].equals(var)) {
                            isValid = false;
                            errors.add("There's already a constant called '" + var + "' at Line: " + token.getLine());
                        }
                    }
                }
            }
            actualVariable.getVariables().get(actualVariable.getVariables().size() - 1)[1] = token.getLexeme();
            getToken();
            arrayVerification(actualVariable.getVariables().get(actualVariable.getVariables().size() - 1));
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

    private void moreNames() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            String type = "";
            if (inMethod) {
                MethodTable m = actualClass.getMethods().get(actualClass.getMethods().size() - 1);
                actualVariable = m.getVariables();
                type = actualVariable.getVariables().get(actualVariable.getVariables().size() - 1)[0];
            } else {
                actualVariable = table.get(table.size() - 1).getVariables();
                type = actualVariable.getVariables().get(actualVariable.getVariables().size() - 1)[0];
            }
            String[] attr = new String[3];
            attr[0] = type;
            attr[2] = "null";
            actualVariable.getVariables().add(attr);
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

    private void parameterDeclaration(MethodTable m) {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            parameterDeclaration2(m);
        }
    }

    private void parameterDeclaration2(MethodTable m) {
        if (verifyType() || token.getAttr_name() == Attribute.ID) {
            if (token.getAttr_name() == Attribute.ID) {
                boolean exists = false;
                for (ClassTable c : table) {
                    if (c.getName().equals(token.getLexeme())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    isValid = false;
                    errors.add("There's no class named '" + token.getLexeme() + "' at method's: '" + m.getName() + "' parameters, at class: '" + actualClass.getName() + "' at Line: " + token.getLine());
                }
            }
            String type = token.getLexeme();
            getToken();
            if (token.getAttr_name() == Attribute.ID) {
                String name = token.getLexeme();
                String[] param = new String[3];
                param[0] = type;
                param[1] = name;
                for (String[] p : m.getParameters()) {
                    if (p[1].equals(name)) {
                        isValid = false;
                        errors.add("There's parameters with the same name at method '" + m.getName() + "' at Class: '" + actualClass.getName() + "'");
                    }
                }
                m.getParameters().add(param);
                getToken();
                arrayVerification(m);
                moreParameters(m);
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

    private void return1() {
        if (token.getAttr_name() == Attribute.RESERVED_WORD && token.getLexeme().equals("return")) {
            if (actualMethod.getType().equals("void")) {
                isValid = false;
                errors.add("Void method '" + actualMethod.getName() + "' has unnacessary return command at class '" + actualClass.getName() + "' at Line " + token.getLine() + ".");
            }
            getToken();
            return2();
        }
    }

    private void return2() {
        if (token.getAttr_name() == Attribute.ID) {
            boolean matches = false;
            if (!actualMethod.getType().equals("void")) {
                for (String[] s : actualMethod.getVariables().getVariables()) {
                    if (s[1].equals(token.getLexeme())) {
                        if (s[0].equals(actualMethod.getType())) {
                            matches = true;
                        } else {
                            matches = true;
                            isValid = false;
                            errors.add("Variable '" + token.getLexeme() + "', isn't of the same type of its method. Line: " + token.getLine());
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : actualMethod.getParameters()) {
                        if (s[1].equals(token.getLexeme())) {
                            if (s[0].equals(actualMethod.getType())) {
                                matches = true;
                            } else {
                                matches = true;
                                isValid = false;
                                errors.add("Variable '" + token.getLexeme() + "' at method parameter, isn't of the same type of its method. Line: " + token.getLine());
                            }
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : actualClass.getVariables().getVariables()) {
                        if (s[1].equals(token.getLexeme())) {
                            if (s[0].equals(actualMethod.getType())) {
                                matches = true;
                            } else {
                                matches = true;
                                isValid = false;
                                errors.add("Variable '" + token.getLexeme() + "' at its Class Variables, isn't of the same type of its method. Line: " + token.getLine());
                            }
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : constants) {
                        if (s[1].equals(token.getLexeme())) {
                            if (s[0].equals(actualMethod.getType())) {
                                matches = true;
                            } else {
                                matches = true;
                                isValid = false;
                                errors.add("Constant '" + token.getLexeme() + "', isn't of the same type of its method. Line: " + token.getLine());
                            }
                        }
                    }
                }
                if (!matches) {
                    isValid = false;
                    errors.add("There's no variable nor constant named '" + token.getLexeme() + "', at Line: " + token.getLine() + " to be returned.");
                }
            }
            String a[] = new String[3];
            a[1] = token.getLexeme();
            a[2] = "null";
            getToken();
            arrayVerification(a);
            if (matches) {
                matches = false;
                for (String[] s : actualMethod.getVariables().getVariables()) {
                    if (s[1].equals(a[1])) {
                        if (s[2].equals(a[2])) {
                            actualMethod.getReturns().add(a[1]);
                            matches = true;
                        } else {
                            matches = true;
                            isValid = false;
                            if (s[2].equals("array")) {
                                errors.add("Variable '" + a[1] + "' at method Variables, is an array. Line: " + token.getLine());
                            } else if (s[2].equals("matrix")) {
                                errors.add("Variable '" + a[1] + "' at method Variables, is a matrix. Line: " + token.getLine());
                            } else {
                                errors.add("Variable '" + a[1] + "' at method Variables, is neither an array nor a matrix. Line: " + token.getLine());
                            }
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : actualMethod.getParameters()) {
                        if (s[1].equals(a[1])) {
                            if (s[2].equals(a[2])) {
                                actualMethod.getReturns().add(a[1]);
                                matches = true;
                            } else {
                                matches = true;
                                isValid = false;
                                if (s[2].equals("array")) {
                                    errors.add("Variable '" + a[1] + "' at method parameters, is an array. Line: " + token.getLine());
                                } else if (s[2].equals("matrix")) {
                                    errors.add("Variable '" + a[1] + "' at method parameters, is a matrix. Line: " + token.getLine());
                                } else {
                                    errors.add("Variable '" + a[1] + "' at method parameters, is neither an array nor a matrix. Line: " + token.getLine());
                                }
                            }
                        }
                    }
                }
                if (!matches) {
                    for (String[] s : actualClass.getVariables().getVariables()) {
                        if (s[1].equals(a[1])) {
                            if (s[2].equals(a[2])) {
                                actualMethod.getReturns().add(a[1]);
                                matches = true;
                            } else {
                                matches = true;
                                isValid = false;
                                if (s[2].equals("array")) {
                                    errors.add("Variable '" + a[1] + "' at class variables, is an array. Line: " + token.getLine());
                                } else if (s[2].equals("matrix")) {
                                    errors.add("Variable '" + a[1] + "' at class variables, is a matrix. Line: " + token.getLine());
                                } else {
                                    errors.add("Variable '" + a[1] + "' at class variables, is neither an array nor a matrix. Line: " + token.getLine());
                                }
                            }
                        }
                    }
                }
            }
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

    private void value() {
        if (token.getAttr_name() == Attribute.STRING || token.getAttr_name() == Attribute.NUMBER || token.getLexeme().equals("true") || token.getLexeme().equals("false")) {
            if (token.getAttr_name() == Attribute.STRING) {
                if (!actualMethod.getType().equals("string")) {
                    isValid = false;
                    errors.add("Return arguments doesn't match with its method type. Line: " + token.getLine() + ".");
                } else {
                    actualMethod.getReturns().add(token.getLexeme());
                }
            } else if (token.getLexeme().equals("true") || token.getLexeme().equals("false")) {
                if (!actualMethod.getType().equals("bool")) {
                    isValid = false;
                    errors.add("Return arguments doesn't match with its method type. Line: " + token.getLine() + ".");
                } else {
                    actualMethod.getReturns().add(token.getLexeme());
                }
            } else if (token.getAttr_name() == Attribute.NUMBER) {
                String value = token.getLexeme();
                if (actualMethod.getType().equals("int")) {
                    try {
                        int test = Integer.parseInt(value);
                        actualMethod.getReturns().add(token.getLexeme());
                    } catch (NumberFormatException e) {
                        isValid = false;
                        errors.add("Incompatible type for value: '" + value + "', the returned value isn't an integer. At Line: " + token.getLine());
                    }
                } else if (actualMethod.getType().equals("float")) {
                    if (value.contains(".")) {
                        try {
                            float test = Float.parseFloat(value);
                            actualMethod.getReturns().add(token.getLexeme());
                        } catch (NumberFormatException e) {
                            isValid = false;
                            errors.add("Incompatible type for value: '" + value + "', the returned value isn't a float. At Line: " + token.getLine());
                        }
                    } else {
                        isValid = false;
                        errors.add("Incompatible type for value: '" + value + "', the returned value isn't a float. At Line: " + token.getLine());
                    }
                }
            }
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

    private void moreParameters(MethodTable m) {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals(",")) {
            getToken();
            parameterDeclaration2(m);
        }
    }

    private void arrayVerification() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            getToken();
            arrayIndex();
            verifyArrayExpression();
            expression.clear();
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

    private void arrayVerification(String[] s) {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            s[2] = "array";
            getToken();
            arrayIndex();
            verifyArrayExpression();
            expression.clear();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
                doubleArray(s);
            } else {
                isValid = false;
                String er = "Expected Token: ']' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void doubleArray(String[] s) {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            s[2] = "matrix";
            getToken();
            arrayIndex();
            verifyArrayExpression();
            expression.clear();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
            } else {
                isValid = false;
                String er = "Expected Token: ']' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void arrayVerification(MethodTable m) {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            String[] s = m.getParameters().get(m.getParameters().size() - 1);
            s[2] = "array";
            getToken();
            arrayIndex();
            verifyArrayExpression();
            expression.clear();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
                doubleArray(m);
            } else {
                isValid = false;
                String er = "Expected Token: ']' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        } else {
            String[] s = m.getParameters().get(m.getParameters().size() - 1);
            s[2] = "null";
        }
    }

    private void doubleArray(MethodTable m) {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            String[] s = m.getParameters().get(m.getParameters().size() - 1);
            s[2] = "matrix";
            getToken();
            arrayIndex();
            verifyArrayExpression();
            expression.clear();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
            } else {
                isValid = false;
                String er = "Expected Token: ']' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private void arrayIndex() {
        if (token.getAttr_name() == Attribute.NUMBER || token.getAttr_name() == Attribute.ID
                || token.getLexeme().equals("true") || token.getLexeme().equals("false")
                || (token.getAttr_name() == Attribute.ARIT_OP && (token.getLexeme().equals("++") || token.getLexeme().equals("--")
                || token.getLexeme().equals("+") || token.getLexeme().equals("-")))
                || (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("("))
                || (token.getAttr_name() == Attribute.LOGICAL_OP && token.getLexeme().equals("!"))) {
            addExp();
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER or NUMBER' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
            while (!token.getLexeme().equals("]") && !EOF) {
                getToken();
            }
        }
    }

    private void doubleArray() {
        if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("[")) {
            getToken();
            arrayIndex();
            verifyArrayExpression();
            expression.clear();
            if (token.getAttr_name() == Attribute.DELIMITER && token.getLexeme().equals("]")) {
                getToken();
            } else {
                isValid = false;
                String er = "Expected Token: ']' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
                errors.add(er);
            }
        }
    }

    private String type() {
        if (verifyType()) {
            String ret = token.getLexeme();
            getToken();
            return ret;
        } else if (token.getAttr_name() == Attribute.ID) {
            String ret = token.getLexeme();
            getToken();
            return ret;
        } else {
            isValid = false;
            String er = "Expected Token: 'IDENTIFIER | string | float | bool | int | void ' -> Received: " + "\'" + token.getLexeme() + "\'" + " at Line: " + token.getLine();
            errors.add(er);
        }
        return null;
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
        this.table.clear();
        this.constants.clear();
        this.actualClass = null;
        this.actualMethod = null;
        this.actualVariable = null;
        this.hasMain = false;
        this.expression.clear();
        this.inConst = false;
        this.inMethod = false;
        this.inVariable = false;
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

    private void verifyConstantsTypes() {
        for (String[] s : constants) {
            String value = s[2];
            if (s[0].equals("int")) {
                try {
                    int test = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    isValid = false;
                    errors.add("Incompatible type for Constant: '" + s[1] + "', the assigned value isn't an integer.");
                }
            } else if (s[0].equals("float")) {
                if (value.contains(".")) {
                    try {
                        float test = Float.parseFloat(value);
                    } catch (NumberFormatException e) {
                        isValid = false;
                        errors.add("Incompatible type for Constant: '" + s[1] + "', the assigned value isn't a float.");
                    }
                } else {
                    isValid = false;
                    errors.add("Incompatible type for Constant: '" + s[1] + "', the assigned value isn't a float.");
                }
            } else if (s[0].equals("bool")) {
                if (value.equals("true") || value.equals("false")) {
                } else {
                    isValid = false;
                    errors.add("Incompatible type for Constant: '" + s[1] + "', the assigned value isn't a bool");
                }
            } else if (s[0].equals("string")) {
                if (value.startsWith("\"") && value.endsWith("\"")) {
                } else {
                    isValid = false;
                    errors.add("Incompatible type for Constant: '" + s[1] + "', the assigned value isn't a string");
                }
            }
        }
    }

    private void verifyArrayExpression() {
        for (Token t : expression) {
            if (t.getAttr_name() == Attribute.NUMBER) {
                String lex = t.getLexeme();
                try {
                    int test = Integer.parseInt(lex);
                } catch (NumberFormatException e) {
                    isValid = false;
                    errors.add("Invalid expression in Array Argument at Line: " + t.getLine());
                }
            } else if (t.getAttr_name() == Attribute.ID) {
                String var = t.getLexeme();
                boolean found = false;
                for (String[] s : constants) {
                    if (s[1].equals(var)) {
                        if (!s[0].equals("int")) {
                            found = true;
                            isValid = false;
                            errors.add("Invalid expression in Array Argument at Line: " + t.getLine() + ", variable '" + s[1] + "' isn't integer.");
                        } else {
                            found = true;
                        }
                    }
                }
                if (!found) {
                    isValid = false;
                    errors.add("There's no constant called '" + var + "' at Line: " + t.getLine());
                }
            } else if ((t.getAttr_name() == Attribute.LOGICAL_OP) || (t.getAttr_name() == Attribute.REL_OP)) {
                isValid = false;
                errors.add("Relational and Logical Operator isn't allowed at Array Argument, Line: " + t.getLine());
            }
        }

    }

    private void checkIfExpressionIsBoolean() {
        if (verifyIfAllTokensAreDeclared()) {

        }
        expression.clear();
    }

    private boolean verifyIfAllTokensAreDeclared() {
        boolean exists = false;
        for (Token t : expression) {
            if (t.getAttr_name() == Attribute.ID) {
                String var = t.getLexeme();
                for (String[] s : actualMethod.getVariables().getVariables()) {
                    if (s[1].equals(var)) {
                        exists = true;
                    }
                }
                if (!exists) {
                    for (String[] s : actualMethod.getParameters()) {
                        if (s[1].equals(var)) {
                            exists = true;
                        }
                    }
                }
                if (!exists) {
                    for (String[] s : actualClass.getVariables().getVariables()) {
                        if (s[1].equals(var)) {
                            exists = true;
                        }
                    }
                }
                if (!exists) {
                    for (String[] s : constants) {
                        if (s[1].equals(var)) {
                            exists = true;
                        }
                    }
                }
                if (!exists) {
                    isValid = false;
                    errors.add("There's neither variable nor constant declared as: '" + var + "'");
                }
            }
        }
        return exists;
    }

    public void writeOutput(File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("teste/output_" + file.getName())))) {
            if (isValid && EOF) {
                bw.write("-> Success on Parsing File!!!");
            } else {
                if (!EOF) {
                    errors.add("-> Unexpected End of File!!!\n-> There are unalized tokens!\n-> Check if are exceeded '}'");
                }
                bw.write("-> Error on Parsing File!!!\n");
                for (String s : errors) {
                    bw.write(s + "\n");
                }
            }
        }
    }
}
