package View;

import Model.LexicalAnalyzer;
import Model.Token;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {

    private static LexicalAnalyzer analyzer = new LexicalAnalyzer();

    public static void main(String[] args) throws IOException {

        /* Try to open 'teste' directory.
        If it doesn't exists, stops execution.*/
        File dir = new File("teste");
        if (!dir.exists()) {
            System.out.println("> There's no directory called \"teste\".");
        } else {
            /* Run the algorithm for all '.txt' in teste directory*/
            for (File files : dir.listFiles()) {
                if (files.getName().endsWith(".txt")) {
                    ArrayList<ArrayList<Token>> ret = analyzer.start(files);
                    ArrayList<Token> tokens = ret.get(0);
                    ArrayList<Token> errors = ret.get(1);
                    
                    analyzer.writeDataOut(files.getName());

                    //System.out.println(ret);
                }
            }
        }
    }

}
