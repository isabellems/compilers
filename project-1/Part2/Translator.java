import java_cup.runtime.*;
import java.io.*;

class Translator {
    public static void main(String[] argv) throws Exception{
        // System.out.println("Please type your arithmethic expression:");
        // System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("Main.java"))));
        Parser p = new Parser(new Scanner(new InputStreamReader(System.in)));
        p.parse();
    }
}