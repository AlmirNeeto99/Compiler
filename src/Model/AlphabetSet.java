package Model;

import java.util.HashSet;

public class AlphabetSet {

    private static final HashSet<Integer> letters = new HashSet();
    private static final HashSet<Integer> numbers = new HashSet();
    private static final HashSet<Integer> symbols = new HashSet();
    

    public AlphabetSet() {
        for (int i = 65; i < 91; i++) {
            letters.add(i);
        }
        for (int i = 97; i < 123; i++) {
            letters.add(i);
        }
        for (int i = 48; i < 58; i++) {
            numbers.add(i);
        }
        symbols.add(32);
        symbols.add(33);
        for(int i = 35; i < 127; i++){
            symbols.add(i);
        }
    }

    public HashSet<Integer> getLetters() {
        return letters;
    }

    public HashSet<Integer> getNumbers() {
        return numbers;
    }

    public HashSet<Integer> getSymbols() {
        return symbols;
    } 
}
