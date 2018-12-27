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

    private final ArrayList<Token> tokens;
    private final ArrayList<Token> errors;
    private final ReservedWords reserved_words;

    private boolean state = true;
    private boolean isComment = false;
    private boolean isBlockComment = false;
    private boolean isString = false;
    private String lexeme = "";

    private final HashSet<Character> delimiters;
    private final HashSet<Integer> symbols;
    private final HashSet<Integer> letters;
    private final HashSet<Integer> numbers;

    public LexicalAnalyzer() {
        AlphabetSet alpha = new AlphabetSet();
        symbols = alpha.getSymbols();
        letters = alpha.getLetters();
        numbers = alpha.getNumbers();
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
        //writeDataOut(file.getName());
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
        isString = false;
        isComment = false;
        isBlockComment = false;
    }

    private void classify(String token, int line) {
        int position = 0;
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
                        lexeme = "\"" + lexeme + "\"";
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
                if (letters.contains((int) actualChar)) {
                    boolean validId = true;
                    do {
                        if (delimiters.contains(actualChar) || isRelational(actualChar)
                                || isLogical(actualChar) || isArithmetic(actualChar) || actualChar == '!') {
                            break;
                        }
                        if (letters.contains((int) actualChar) || numbers.contains((int) actualChar) || actualChar == '_') {
                            lexeme = lexeme + actualChar;
                        } else {
                            state = false;
                            validId = false;
                            lexeme = lexeme + actualChar;
                        }
                        if (position + 1 < token.length()) {
                            actualChar = token.charAt(++position);
                        } else {
                            position++;
                        }
                    } while (position < token.length());
                    if (validId) {
                        if (isReservedWord(lexeme)) {
                            createToken(lexeme, line, "RESERVED_WORD");
                            lexeme = "";
                        } else {
                            createToken(lexeme, line, "ID");
                            lexeme = "";
                        }
                    } else {
                        state = false;
                        createToken(lexeme, line, "BAD_ID_FORMATION");
                        lexeme = "";
                    }
                } else if (numbers.contains((int) actualChar)) {
                    boolean validNumber = true;
                    boolean dot = false;
                    do {
                        if (delimitersWithoutDot(actualChar) || isRelational(actualChar)
                                || isLogical(actualChar) || isArithmetic(actualChar) || actualChar == '!') {
                            break;
                        }
                        if (numbers.contains((int) actualChar) || actualChar == '.') {
                            if (actualChar == '.') {
                                if (!dot) {
                                    dot = true;
                                } else {
                                    validNumber = false;
                                }
                            }
                            lexeme = lexeme + actualChar;
                        } else {
                            state = false;
                            validNumber = false;
                            lexeme = lexeme + actualChar;
                        }
                        if (position + 1 < token.length()) {
                            actualChar = token.charAt(++position);
                        } else {
                            position++;
                        }
                    } while (position < token.length());
                    if (validNumber) {
                        Token prev = null;
                        if (tokens.size() > 0) {
                            prev = tokens.get(tokens.size() - 1);
                            if (prev.getLexeme().equals("-") && prev.getLine() == line) {
                                tokens.remove(tokens.size() - 1);
                                createToken("-" + lexeme, line, "NUMBER");
                            } else {
                                createToken(lexeme, line, "NUMBER");
                            }
                            lexeme = "";
                        }
                    } else {
                        state = false;
                        createToken(lexeme, line, "BAD_NUMBER_FORMATION");
                        lexeme = "";
                    }
                } else {
                    switch (actualChar) {
                        case '+': {
                            if (hasNext(token, position)) {
                                char nextChar = token.charAt(position + 1);
                                if (nextChar == '+') {
                                    createToken("++", line, "ARIT.OP");
                                    position += 2;
                                } else {
                                    createToken("+", line, "ARIT.OP");
                                    position += 1;
                                }
                            } else {
                                createToken("+", line, "ARIT.OP");
                                position += 1;
                            }
                            break;
                        }
                        case '-': {
                            if (hasNext(token, position)) {
                                char nextChar = token.charAt(position + 1);
                                if (nextChar == '-') {
                                    createToken("--", line, "ARIT.OP");
                                    position += 2;
                                } else {
                                    createToken("-", line, "ARIT.OP");
                                    position += 1;
                                }
                            } else {
                                createToken("-", line, "ARIT.OP");
                                position += 1;
                            }
                            break;
                        }
                        case '*': {
                            createToken("*", line, "ARIT.OP");
                            position += 1;
                            break;
                        }
                        case '/': {
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
                                        createToken("/", line, "ARIT.OP");
                                        position++;
                                        break;
                                }
                            } else {
                                createToken("/", line, "ARIT.OP");
                                position++;
                            }
                            break;
                        }
                        case '!': {
                            if (hasNext(token, position)) {
                                char nextChar = token.charAt(position + 1);
                                if (nextChar == '=') {
                                    position += 2;
                                    createToken("!=", line, "REL.OP");
                                } else {
                                    position++;
                                    createToken("!", line, "LOGICAL.OP");
                                }
                            } else {
                                createToken("!", line, "LOGICAL.OP");
                                position++;
                            }
                            break;
                        }
                        case '&': {
                            if (hasNext(token, position)) {
                                char nextChar = token.charAt(position + 1);
                                if (nextChar == '&') {
                                    position += 2;
                                    createToken("&&", line, "LOGICAL.OP");
                                } else {
                                    position++;
                                    createToken("&", line, "BAD_LOGICAL_OP");
                                    state = false;
                                }
                            } else {
                                position++;
                                createToken("&", line, "BAD_LOGICAL_OP");
                                state = false;
                            }
                            break;
                        }
                        case '|': {
                            if (hasNext(token, position)) {
                                char nextChar = token.charAt(position + 1);
                                if (nextChar == '|') {
                                    position += 2;
                                    createToken("||", line, "LOGICAL.OP");
                                } else {
                                    position++;
                                    createToken("|", line, "BAD_LOGICAL_OP");
                                    state = false;
                                }
                            } else {
                                position++;
                                createToken("|", line, "BAD_LOGICAL_OP");
                                state = false;
                            }
                            break;
                        }
                        case '<': {
                            if (hasNext(token, position)) {
                                char nextChar = token.charAt(position + 1);
                                if (nextChar == '=') {
                                    position += 2;
                                    createToken("<=", line, "REL.OP");
                                } else {
                                    createToken("<", line, "REL.OP");
                                    position++;
                                }
                            } else {
                                position++;
                                createToken("<", line, "REL.OP");
                            }
                            break;
                        }
                        case '>':
                            if (hasNext(token, position)) {
                                char nextChar = token.charAt(position + 1);
                                if (nextChar == '=') {
                                    position += 2;
                                    createToken(">=", line, "REL.OP");
                                } else {
                                    createToken(">", line, "REL.OP");
                                    position++;
                                }
                            } else {
                                position++;
                                createToken(">", line, "REL.OP");
                            }
                            break;
                        case '=':
                            if (hasNext(token, position)) {
                                char nextChar = token.charAt(position + 1);
                                if (nextChar == '=') {
                                    position += 2;
                                    createToken("==", line, "REL.OP");
                                } else {
                                    createToken("=", line, "REL.OP");
                                    position++;
                                }
                            } else {
                                position++;
                                createToken("=", line, "REL.OP");
                            }
                            break;
                        // String started.
                        case '\"':
                            lexeme = "";
                            isString = true;
                            createToken(lexeme, line, "NULL");
                            position++;
                            break;
                        default: {
                            if (delimiters.contains(actualChar)) {
                                createToken("" + actualChar, line, "DELIMITER");
                                position++;
                            }
                            break;
                        }
                    }
                }
            }
        } while (position < token.length());
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

    private boolean delimitersWithoutDot(char c) {
        return c == ';' || c == ',' || c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}';
    }

    private boolean isReservedWord(String word) {
        return reserved_words.isReservedWord(word);
    }

    private boolean isArithmetic(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean isRelational(char c) {
        return c == '=' || c == '<' || c == '>';
    }

    private boolean isLogical(char c) {
        return c == '&' || c == '|';
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
