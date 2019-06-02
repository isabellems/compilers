import java.io.PrintWriter;
import java.io.File;
import java.util.*;

class VTable{
  
  // public static String fileName;
  public static Map<String, ClassInfo> ClassTable;
  public static FunctionInfo MainTable;
  public static PrintWriter printWriter;

  public VTable(String _fileName, Map<String, ClassInfo> _classTable, FunctionInfo _mainTable) throws Exception{
	  ClassTable = _classTable;
	  MainTable = _mainTable;
    int index = _fileName.lastIndexOf("/");
    if(index != -1){
      _fileName = _fileName.substring(index);
    }
    index = _fileName.lastIndexOf(".");
    if(index != -1){
      _fileName = _fileName.substring(0, index);
    }
    File file = new File("./outputFiles/" + _fileName + ".txt");
    file.getParentFile().mkdirs();
    printWriter = new PrintWriter(file);
  }

  public boolean functExists(String name, ClassInfo ownerInfo){
    FunctionInfo funct = null;
	String parent = ownerInfo.parent;
	Boolean exists = false;
	while(parent != null){
		ClassInfo parInfo = ClassTable.get(parent);
	    if(parInfo.functions.containsKey(name)){
	      funct = parInfo.functions.get(name);
	      exists = true;
	      return true;
	    }
	    parent = parInfo.parent;
	}
    return false;
  }

  public void makeVTable(){
      Iterator<Map.Entry<String,ClassInfo>> classes = ClassTable.entrySet().iterator();
      Map.Entry<String,ClassInfo> cl = classes.next(); //skip main class
      while(classes.hasNext()){	
      	cl = classes.next();
      	printWriter.println("-----------Class " + cl.getKey() + "-----------");
      	ClassInfo info = cl.getValue();
      	Iterator<Map.Entry<String,String>> members = info.members.entrySet().iterator();
      	Iterator<Map.Entry<String,FunctionInfo>> functions = info.functions.entrySet().iterator();
      	printWriter.println("--Variables---");
  	 	if(info.parent != null){
  			info.memOffset = ClassTable.get(info.parent).memOffset;
  			info.funOffset = ClassTable.get(info.parent).funOffset;
  		}
      	while(members.hasNext()){
      		Map.Entry<String, String> mem = members.next();
      		printWriter.print(info.name + "." + mem.getKey() + " : ");

  			printWriter.print(info.memOffset + "\n");
  			if(mem.getValue().equals("int")){
  				info.memOffset += 4;
  			}
  			else if(mem.getValue().equals("boolean")){
  				info.memOffset += 1;
  			}
  			else{
  				info.memOffset += 8;
  			}	
      	}

      	printWriter.println("---Methods---");
      	while(functions.hasNext()){
      		Map.Entry<String, FunctionInfo> fun = functions.next();
      		if(info.parent == null || !functExists(fun.getKey(), info)){
      			printWriter.print(info.name + "." + fun.getKey() + " : ");
	  			printWriter.print(info.funOffset + "\n");
				info.funOffset += 8;
      		}
      	}

      	printWriter.println("");

      }
      printWriter.close(); 
  }

}