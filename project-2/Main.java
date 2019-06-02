import syntaxtree.*;
import visitor.*;
import java.io.*;

class Main {
    public static void main (String [] args){
	if(args.length < 1){
	    System.err.println("Usage: java Driver <inputFile>");
	    System.exit(1);
	}
	FileInputStream fis = null;
	for(int i = 0 ; i < args.length ; i++){
		try{
		    fis = new FileInputStream(args[i]);
		    MiniJavaParser parser = new MiniJavaParser(fis);
		    System.out.println("\nFile " + args[i] + ":");
		    System.err.println("Program parsed successfully.");
		    SymbolTable table = new SymbolTable();
		    MyTypeChecker checker = new MyTypeChecker(table.ClassTable, table.MainTable);
		    VTable vTable = new VTable(args[i], table.ClassTable, table.MainTable);
		    Goal root = parser.Goal();
		    root.accept(table, null);
		    root.accept(checker, null);
		    vTable.makeVTable();
		}
		catch(ParseException ex){
		    System.out.println(ex.getMessage());
		}
		catch(FileNotFoundException ex){
		    System.err.println(ex.getMessage());
		}
		catch(Exception ex){
		    System.err.println(ex.getMessage());
		}
		finally{
		  try{
			if(fis != null) fis.close();
		    }
		    catch(IOException ex){
				System.err.println(ex.getMessage());
		    }
		  }
	    }
	}
}