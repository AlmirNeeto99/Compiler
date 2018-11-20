package Util;

import java.util.Arrays;
import java.util.HashSet;

public class ReservedWords {

    private final String[] words_array = {"class", "const", "variables", "method", "return", "main", "if", "then", "else",
        "while", "read", "write", "void", "int", "float", "bool", "string", "true", "false", "extends"};

    private final HashSet<String> words;

    public ReservedWords() {
        words = new HashSet();
        words.addAll(Arrays.asList(words_array));
    }

    public boolean isReservedWord(String check) {
        return words.contains(check);
    }

}
