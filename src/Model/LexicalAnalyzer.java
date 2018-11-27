package Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import Util.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;

public class LexicalAnalyzer {

    private ArrayList<Token> tokens;
    private ArrayList<Token> errors;
    private ReservedWords reserved_words;

    private boolean state = true;
    private boolean isComment = false;
    private boolean isBlockComment = false;
    private boolean isString = false;
    private String lexeme = "";

    private HashSet<Character> delimiters;
    private final HashSet<Integer> symbols = AlphabetSet.getSymbols();
    private final HashSet<Integer> letters = AlphabetSet.getLetters();
    private final HashSet<Integer> numbers = AlphabetSet.getNumbers();

    public LexicalAnalyzer() {
        tokens = new ArrayList();
        errors = new ArrayList();
        reserved_words = new ReservedWords();
        delimiters = new HashSet();
        state = true;
        addDelimiters();
    }

    public ArrayList<ArrayList<Token>> start(File file) throws IOException {
        analyze(file);
        ArrayList<ArrayList<Token>> ret = new ArrayList<>(2);
        ret.add(tokens);
        ret.add(errors);
        writeDataOut(file.getName());
        return ret;
    }

    private void analyze(File file) throws FileNotFoundException, IOException {
        int actualLine = 1;
        BufferedReader br = new BufferedReader(new FileReader(file));
        String lex;
        StringTokenizer token;
        while ((lex = br.readLine()) != null) { // Read a single line, if itsn't the EOF
            if (lex.trim().length() == 0) {
                //If the line is empty increments line number and go to the next line.
                actualLine++;
            } else {
                token = new StringTokenizer(lex);
                while (token.hasMoreTokens()) {
                    /* Read a token in line, splitted by whitespace*/
                    String nextToken = token.nextToken();
                    classify(nextToken, actualLine);
                    if (isString) {
                        lexeme = lexeme + " ";
                    }
                }
                actualLine++;
                /*If a String starts and its line end, an error is created.*/
                if (isString) {
                    state = false;
                    Token changes = tokens.get(tokens.size() - 1);
                    lexeme = lexeme.trim();
                    changes.setAttr_name(Attribute.BAD_STRING);
                    changes.setLexeme(lexeme);
                    isString = false;
                    lexeme = "";
                    tokens.remove(changes);
                    errors.add(changes);
                }
                isComment = false;
                isString = false;
            }
        }
        /*If a BlockComment starts and its file end, an error is created.*/
        if (isBlockComment) {
            state = false;
            Token comment_t = new Token("", Attribute.COMMENT_WITHOUT_END, actualLine);
            errors.add(comment_t);
        }
        actualLine = 1;
        isString = false;
        isComment = false;
        isBlockComment = false;
    }

    private void classify(String token, int line) {

        int position = 0;
        IdAutomaton id = new IdAutomaton();
        NumberAutomaton number = new NumberAutomaton();

        boolean isId = false;
        boolean isNumber = false;
        boolean negativeNumber = false;
        boolean isBadFormedString = false;

        do {
            char actualChar = token.charAt(position);
            if (isBlockComment) {
                switch (actualChar) {
                    case '*':
                        if (hasNext(token, position)) {
                            char nextChar = token.charAt(position + 1);
                            if (nextChar == '/') {
                                position += 2;
                                isBlockComment = false;
                            } else {
                                position++;
                            }
                        } else {
                            position++;
                        }
                    default:
                        position++;
                        break;
                }
            } else if (isString) {
                if (actualChar == '\"') { //If the String ends, create a token for it.
                    Token changes = tokens.get(tokens.size() - 1);
                    if (isBadFormedString) { //If the string has any character that isn't allowed, create a BAD_STRING token.
                        lexeme = lexeme.trim();
                        changes.setAttr_name(Attribute.BAD_STRING);
                        changes.setLexeme(lexeme);
                        isString = false;
                        isBadFormedString = false;
                        lexeme = "";
                        tokens.remove(changes);
                        errors.add(changes);
                        position++;
                        state = false;
                    } else {
                        lexeme = lexeme.trim();
                        lexeme = "\""+lexeme+"\"";
                        changes.setAttr_name(Attribute.STRING);
                        changes.setLexeme(lexeme);
                        isString = false;
                        lexeme = "";
                        position++;
                    }
                } else { //If it's a string, verifies if actualChar is allowed in the String.
                    if (symbols.contains((int) actualChar)) {
                        lexeme = lexeme + actualChar;
                        position++;
                    } else if (letters.contains((int) actualChar)) {
                        lexeme = lexeme + actualChar;
                        position++;
                    } else if (numbers.contains((int) actualChar)) {
                        lexeme = lexeme + actualChar;
                        position++;
                    } else {
                        lexeme = lexeme + actualChar;
                        position++;
                        isBadFormedString = true;
                        state = false;
                    }
                }
            } else if (isComment) { //Ignore all character until the end of the line.
                position++;
            } else {
                /* Every case verify if there's something before it.
                If there's something, create a token with it and starts classifying.
                Otherwise, classify.*/
                switch (actualChar) {
                    /*ARITHMETHIC OPERATORS*/
                    case '+':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        }
                        if (hasNext(token, position)) {
                            char nextChar = token.charAt(position + 1);
                            if (nextChar == '+') {
                                createToken(actualChar + "" + nextChar, line, "ARIT.OP");
                                position += 2;
                            } else {
                                createToken(actualChar + "", line, "ARIT.OP");
                                position++;
                            }
                        } else {
                            createToken(actualChar + "", line, "ARIT.OP");
                            position++;
                        }
                        break;
                    case '*':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        }
                        createToken(actualChar + "", line, "ARIT.OP");
                        position++;
                        break;
                    case '-':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        } else {
                            Token previous = null;
                            if (tokens.size() > 0) {
                                previous = tokens.get(tokens.size() - 1);
                            }
                            if (hasNext(token, position)) {
                                char nextChar = token.charAt(position + 1);
                                if (nextChar == '-') {
                                    createToken(actualChar + "" + nextChar, line, "ARIT.OP");
                                    position += 2;
                                } else if (numbers.contains((int) nextChar)) {
                                    negativeNumber = true;
                                    lexeme = lexeme + actualChar + nextChar;
                                    position += 2;
                                    number.testChar(actualChar);
                                    number.testChar(nextChar);
                                    isNumber = number.canBeValid();
                                    id.testChar(actualChar);
                                    id.testChar(nextChar);
                                    isId = id.canBeValid();
                                } else {
                                    createToken(actualChar + "", line, "ARIT.OP");
                                    position++;
                                }
                            } else {
                                if (previous != null) {
                                    if (previous.getAttr_name() == Attribute.ID || previous.getAttr_name() == Attribute.NUMBER || previous.getAttr_name() == Attribute.BAD_ID_FORMATION || previous.getAttr_name() == Attribute.BAD_NUMBER_FORMATION) {
                                        createToken(actualChar + "", line, "ARIT.OP");
                                        position++;
                                    } else {
                                        lexeme = "-";
                                        number.testChar(actualChar);
                                        isNumber = number.canBeValid();
                                        negativeNumber = true;
                                        position++;
                                        id.testChar(actualChar);
                                        isId = id.canBeValid();
                                    }
                                } else {
                                    lexeme = "-";
                                    number.testChar(actualChar);
                                    isNumber = number.canBeValid();
                                    negativeNumber = true;
                                    position++;
                                    id.testChar(actualChar);
                                    isId = id.canBeValid();
                                }
                            }
                        }
                        break;
                    /*LOGICAL OPERATORS*/
                    case '/':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        }
                        if (hasNext(token, position)) {
                            char nextChar = token.charAt(position + 1);
                            switch (nextChar) {
                                case '*':
                                    position += 2;
                                    isBlockComment = true;
                                    break;
                                case '/':
                                    isComment = true;
                                    position += 2;
                                    break;
                                default:
                                    createToken(actualChar + "", line, "ARIT.OP");
                                    position++;
                                    break;
                            }
                        } else {
                            createToken(actualChar + "", line, "ARIT.OP");
                            position++;
                        }
                        break;
                    case '!':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        }
                        if (hasNext(token, position)) {
                            char nextChar = token.charAt(position + 1);
                            if (nextChar == '=') {
                                position += 2;
                                createToken(actualChar + "" + nextChar, line, "REL.OP");
                            } else {
                                position++;
                                createToken(actualChar + "", line, "LOGICAL.OP");
                            }
                        } else {
                            createToken(actualChar + "", line, "LOGICAL.OP");
                            position++;
                        }
                        break;
                    case '&':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        }
                        if (hasNext(token, position)) {
                            char nextChar = token.charAt(position + 1);
                            if (nextChar == '&') {
                                position += 2;
                                createToken(actualChar + "" + nextChar, line, "LOGICAL.OP");
                            } else {
                                position++;
                                createToken(actualChar + "", line, "BAD_LOGICAL_OP");
                                state = false;

                            }
                        } else {
                            position++;
                            createToken(actualChar + "", line, "BAD_LOGICAL_OP");
                            state = false;

                        }
                        break;
                    /*RELATIONAL OPERATORS*/
                    case '|':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        }
                        if (hasNext(token, position)) {
                            char nextChar = token.charAt(position + 1);
                            if (nextChar == '|') {
                                position += 2;
                                createToken(actualChar + "" + nextChar, line, "REL.OP");
                            } else {
                                position++;
                                createToken(actualChar + "", line, "BAD_RELATIONAL_OP");
                                state = false;

                            }
                        } else {
                            position++;
                            createToken(actualChar + "", line, "BAD_RELATIONAL_OP");
                            state = false;

                        }
                        break;
                    case '<':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        }
                        if (hasNext(token, position)) {
                            char nextChar = token.charAt(position + 1);
                            if (nextChar == '=') {
                                position += 2;
                                createToken(actualChar + "" + nextChar, line, "REL.OP");
                            } else {
                                createToken(actualChar + "", line, "REL.OP");
                                position++;
                            }
                        } else {
                            position++;
                            createToken(actualChar + "", line, "REL.OP");
                        }
                        break;
                    case '>':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        }
                        if (hasNext(token, position)) {
                            char nextChar = token.charAt(position + 1);
                            if (nextChar == '=') {
                                position += 2;
                                createToken(actualChar + "" + nextChar, line, "REL.OP");
                            } else {
                                createToken(actualChar + "", line, "REL.OP");
                                position++;
                            }
                        } else {
                            position++;
                            createToken(actualChar + "", line, "REL.OP");
                        }
                        break;
                    case '=':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                            lexeme = "";
                        }
                        if (hasNext(token, position)) {
                            char nextChar = token.charAt(position + 1);
                            if (nextChar == '=') {
                                position += 2;
                                createToken(actualChar + "" + nextChar, line, "REL.OP");
                            } else {
                                createToken(actualChar + "", line, "REL.OP");
                                position++;
                            }
                        } else {
                            position++;
                            createToken(actualChar + "", line, "REL.OP");
                        }
                        break;
                    // String started.
                    case '\"':
                        if (lexeme.length() > 0) {
                            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                        }
                        lexeme = "";
                        isString = true;
                        createToken(lexeme, line, "NULL");
                        position++;
                        break;
                    default:
                        if (delimiters.contains(actualChar)) {
                            if (lexeme.length() > 0) {
                                if (negativeNumber) {
                                    if (isNumber) {
                                        if (actualChar == '.') {
                                            lexeme = lexeme + '.';
                                            position++;
                                            number.testChar(actualChar);
                                            isNumber = number.canBeValid();
                                            negativeNumber = number.canBeValid();
                                        } else {
                                            createToken(lexeme, line, "NUMBER");
                                            lexeme = "";
                                            isNumber = false;
                                            negativeNumber = false;
                                            createToken(actualChar + "", line, "DELIMITER");
                                            position++;
                                            number.reset();
                                            id.reset();
                                        }
                                    } else {
                                        createToken(lexeme, line, "BAD_NUMBER_FORMATION");
                                        lexeme = "";
                                        createToken(actualChar + "", line, "DELIMITER");
                                        position++;
                                        isNumber = false;
                                        negativeNumber = false;
                                        number.reset();
                                        id.reset();
                                    }
                                } else if (isId) {
                                    if (isReservedWord(lexeme)) {
                                        createToken(lexeme, line, "RESERVED_WORD");
                                        lexeme = "";
                                        isId = false;
                                        createToken(actualChar + "", line, "DELIMITER");
                                        position++;
                                        id.reset();
                                        number.reset();
                                    } else {
                                        createToken(lexeme, line, "ID");
                                        lexeme = "";
                                        isId = false;
                                        createToken(actualChar + "", line, "DELIMITER");
                                        position++;
                                        id.reset();
                                        number.reset();
                                    }
                                } else if (isNumber) {
                                    if (actualChar == '.') {
                                        lexeme = lexeme + '.';
                                        position++;
                                        number.testChar(actualChar);
                                        isNumber = number.canBeValid();
                                    } else {
                                        createToken(lexeme, line, "NUMBER");
                                        lexeme = "";
                                        isNumber = false;
                                        createToken(actualChar + "", line, "DELIMITER");
                                        position++;
                                        number.reset();
                                        id.reset();
                                    }
                                } else {
                                    if (id.transition() > number.transitions()) {
                                        createToken(lexeme, line, "BAD_ID_FORMATION");
                                        state = false;
                                        lexeme = "";
                                        createToken(actualChar + "", line, "DELIMITER");
                                        position++;
                                        number.reset();
                                        id.reset();
                                    } else if (id.transition() < number.transitions()) {
                                        createToken(lexeme, line, "BAD_NUMBER_FORMATION");

                                        state = false;
                                        lexeme = "";
                                        createToken(actualChar + "", line, "DELIMITER");
                                        position++;
                                        number.reset();
                                        id.reset();
                                    } else {
                                        createToken(lexeme, line, "BAD_FORMATION");
                                        state = false;
                                        lexeme = "";
                                        createToken(actualChar + "", line, "DELIMITER");
                                        position++;
                                        number.reset();
                                        id.reset();
                                    }
                                }
                            } else {
                                createToken(actualChar + "", line, "DELIMITER");
                                position++;
                                number.reset();
                                id.reset();
                            }
                        } else {
                            id.testChar(actualChar);
                            isId = id.canBeValid();
                            number.testChar(actualChar);
                            isNumber = number.canBeValid();
                            position++;
                            lexeme = lexeme + actualChar;
                        }
                        break;
                }
            }
        } while (position < token.length());
        if (negativeNumber) {
            if (lexeme.length() == 1) {
                createToken(lexeme, line, "ARIT.OP");
                lexeme = "";
                number.reset();
                id.reset();
            } else if (lexeme.length() > 1) {
                classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
                lexeme = "";
            }
        } /*If the lexeme isn't a String or a Comment or a Block comment
        The lexeme will be classyfied analyzing the number of transitions in each automaton.*/ else if (!isString && !isComment && !isBlockComment) {
            classifyPreviousLexeme(lexeme, line, isId, isNumber, id, number);
            lexeme = "";
        }
    }

    /*Verify if there's more char after actualChar*/
    private boolean hasNext(String text, int nextPosition) {
        return nextPosition + 1 < text.length();
    }

    /*Create a token and put it in List.*/
    private void createToken(String lexeme, int line, String tokenType) {
        if (lexeme.length() > 0) {
            Attribute attr = getTokenType(tokenType);
            Token token = new Token(lexeme, attr, line);
            if (tokenType.equals("BAD_FORMATION")) {
                errors.add(token);
            } else if (tokenType.equals("BAD_NUMBER_FORMATION")) {
                errors.add(token);
            } else if (tokenType.equals("BAD_ID_FORMATION")) {
                errors.add(token);
            } else if (tokenType.equals("BAD_STRING")) {
                errors.add(token);
            } else if (tokenType.equals("BAD_RELATIONAL_OP")) {
                errors.add(token);
            } else if (tokenType.equals("BAD_LOGICAL_OP")) {
                errors.add(token);
            } else if (tokenType.equals("ERROR")) {
                errors.add(token);
            } else {
                tokens.add(token);
            }
        } else if (tokenType.equals("STRING") || tokenType.equals("NULL")) {
            Attribute attr = getTokenType(tokenType);
            Token token = new Token(lexeme, attr, line);
            tokens.add(token);
        }
    }

    private Attribute getTokenType(String type) {
        switch (type) {
            case "ARIT.OP":
                return Attribute.ARIT_OP;
            case "REL.OP":
                return Attribute.REL_OP;
            case "LOGICAL.OP":
                return Attribute.LOGICAL_OP;
            case "DELIMITER":
                return Attribute.DELIMITER;
            case "NUMBER":
                return Attribute.NUMBER;
            case "ID":
                return Attribute.ID;
            case "RESERVED_WORD":
                return Attribute.RESERVED_WORD;
            case "COMMENT":
                return Attribute.COMMENT;
            case "STRING":
                return Attribute.STRING;
            case "BAD_STRING":
                return Attribute.BAD_STRING;
            case "BAD_RELATIONAL_OP":
                return Attribute.BAD_RELATIONAL_OP;
            case "BAD_LOGICAL_OP":
                return Attribute.BAD_LOGICAL_OP;
            case "BAD_ID_FORMATION":
                return Attribute.BAD_ID_FORMATION;
            case "BAD_NUMBER_FORMATION":
                return Attribute.BAD_NUMBER_FORMATION;
            case "BAD_FORMATION":
                return Attribute.BAD_FORMATION;
            default:
                break;
        }
        return null;
    }

    private void addDelimiters() {
        char[] delimiter = {';', ',', '(', ')', '[', ']', '{', '}', '.'};

        for (int i = 0; i < delimiter.length; i++) {
            delimiters.add(delimiter[i]);
        }
    }

    private boolean isReservedWord(String word) {
        return reserved_words.isReservedWord(word);
    }

    private void classifyPreviousLexeme(String lexeme, int line, boolean isId, boolean isNumber, IdAutomaton id, NumberAutomaton number) {
        if (isId) {
            if (isReservedWord(lexeme)) {
                createToken(lexeme, line, "RESERVED_WORD");
                isId = false;
            } else {
                createToken(lexeme, line, "ID");
                isId = false;
            }
        } else if (isNumber) {
            if (number.getActualState().isFinal()) {
                createToken(lexeme, line, "NUMBER");
            } else {
                createToken(lexeme, line, "BAD_NUMBER_FORMATION");
                state = false;
            }
        } else {
            if (id.transition() > number.transitions()) {
                createToken(lexeme, line, "BAD_ID_FORMATION");
                state = false;
            } else if (id.transition() < number.transitions()) {
                createToken(lexeme, line, "BAD_NUMBER_FORMATION");
                state = false;
            } else {
                if (lexeme.length() > 0) {
                    createToken(lexeme, line, "BAD_FORMATION");
                    state = false;
                }
            }
        }
        id.reset();
        number.reset();
    }

    public void writeDataOut(String entry_name) throws IOException {
        File out = new File("output");

        if (!out.exists()) {
            out.mkdir();
        }

        File file_out = new File("output/out_" + entry_name);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file_out))) {
            for (Token token_out : tokens) {
                bw.write(token_out.toString());
                bw.write("\n");
            }
            if (state) {
                bw.write("\n\nSUCCESS ON PARSING FILE!");
            } else {
                bw.write("\n\n");
                bw.write("Errors:\n");
                for (Token token_out : errors) {
                    bw.write(token_out.toString());
                    bw.write("\n");
                }
            }
        }
    }

    public void clear() {
        tokens.clear();
        errors.clear();
    }
}
