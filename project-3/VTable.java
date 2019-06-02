import java.io.PrintWriter;
import java.io.File;
import java.util.*;

class VTable{
  
  // public static String fileName;
  public static Map<String, ClassInfo> ClassTable;
  public static FunctionInfo MainTable;
  public static Map<String, Offsets> table;
  public static PrintWriter printWriter;

  public VTable(String _fileName, Map<String, ClassInfo> _classTable, FunctionInfo _mainTable) throws Exception{
	  ClassTable = _classTable;
	  MainTable = _mainTable;
    table = new LinkedHashMap<>();
    int index = _fileName.lastIndexOf("/");
    if(index != -1){
      _fileName = _fileName.substring(index);
    }
    index = _fileName.lastIndexOf(".");
    if(index != -1){
      _fileName = _fileName.substring(0, index);
    }
  }

  public int getType(String type){
    if (type.equals("boolean")){
      return 1;
    }
    else if(type.equals("int")){
      return 4;
    }
    else{
      return 8;
    }
  }

  public boolean functExists(String name, ClassInfo ownerInfo){
    FunctionInfo funct = null;
  	String parent = ownerInfo.parent;
  	while(parent != null){
  		ClassInfo parInfo = ClassTable.get(parent);
  	    if(parInfo.functions.containsKey(name)){
  	      funct = parInfo.functions.get(name);
  	      // exists = true;
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
      	ClassInfo info = cl.getValue();
        fillTable(info);
      }

    // Iterator<Map.Entry<String, Offsets>> off = table.entrySet().iterator();
    // while (off.hasNext()) {
    //     Map.Entry<String, Offsets> member = off.next();
    //     member.getValue().print();
    // }

  }

  public void fillTable(ClassInfo info){
    Iterator<Map.Entry<String,String>> members = info.members.entrySet().iterator();
    Iterator<Map.Entry<String,FunctionInfo>> functions = info.functions.entrySet().iterator();
    Offsets tableMembers = new Offsets();

    if(info.parent != null){
      info.memOffset = ClassTable.get(info.parent).memOffset;
      info.funOffset = ClassTable.get(info.parent).funOffset;
    }
    while(members.hasNext()){
      Map.Entry<String, String> mem = members.next();
      tableMembers.members.put(mem.getKey(), info.memOffset);
      tableMembers.size += getType(mem.getValue());
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
    table.put(info.name, tableMembers);

  }

  public Integer getMember(String member, String owner){
    ClassInfo cinfo = ClassTable.get(owner);
    if(cinfo.members.containsKey(member)){
      Offsets offs = table.get(owner);
      return offs.members.get(member);
    }
    else{
      String parent = cinfo.parent;
      while(parent != null){
        cinfo = ClassTable.get(parent);
        if(cinfo.members.containsKey(member)){
           Offsets offs = table.get(owner);
          return offs.members.get(member);
        }
        parent = cinfo.parent;
      }
    }
    return null;
  }

  public Integer getFunction(String funct, String owner){
    ClassInfo cinfo = ClassTable.get(owner);
    Offsets offs = table.get(owner);
    Integer offset = null;
    if(offs.functs.containsKey(funct)){
      return offs.functs.get(funct);
    }
    else{
      String parent = cinfo.parent;
      while(parent != null){
        offs = table.get(parent);
        cinfo = ClassTable.get(parent);
        if(offs.functs.containsKey(funct)){
          offset = offs.functs.get(funct);
        }
        parent = cinfo.parent;
      }
    }
    return offset;
  }

  public int getSize(String name){
    ClassInfo cinfo = ClassTable.get(name);
    Offsets offs = table.get(name);
    int size = offs.size;
    String parent = cinfo.parent;
    while(parent != null){
      cinfo = ClassTable.get(parent);
      offs = table.get(parent);
      size += offs.size;
      parent = cinfo.parent;
    }
    return size;
  }

  public int getFunctionsLen(String name){
    Offsets offs = table.get(name);
    return offs.functLen;
  }

  public List<FunctionInfo> getFunctionArray(String name){
    String clName = name;
    List<String> writtenFuncts = new ArrayList<String>();
    Map<String, FunctionInfo> tempFuncts = new LinkedHashMap<String, FunctionInfo>();
    List<FunctionInfo> retList = new ArrayList<FunctionInfo>();
    Stack<String> clVisited = new Stack<String>();
    while(name != null){
      ClassInfo cinfo = ClassTable.get(name);
      clVisited.push(cinfo.name);
      if(cinfo.functions.size() > 0){
        Iterator<Map.Entry<String, FunctionInfo>> functs = cinfo.functions.entrySet().iterator();
        while(functs.hasNext()){
          Map.Entry<String, FunctionInfo> fm = functs.next();
          if(!writtenFuncts.contains(fm.getKey())){
            tempFuncts.put(fm.getKey(), fm.getValue());
            writtenFuncts.add(fm.getKey());
          }
        }
        // for(int i = tempFuncts.size() - 1; i >= 0 ; i--){
        //   functList.add(tempFuncts.get(i));
        // }
      }
      name = cinfo.parent;
    }
    Offsets offs = table.get(clName);
    int offset = 0;
    writtenFuncts = new ArrayList<String>();
    while(!clVisited.empty()){
      ClassInfo cinfo = ClassTable.get(clVisited.pop());
      Iterator<Map.Entry<String, FunctionInfo>> functs = cinfo.functions.entrySet().iterator();
      while(functs.hasNext()){
        Map.Entry<String, FunctionInfo> fm = functs.next();
        if(!writtenFuncts.contains(fm.getKey())){
          if(tempFuncts.containsKey(fm.getKey())){
            retList.add(tempFuncts.get(fm.getKey()));
          }
          else{
            retList.add(fm.getValue());
          }
          offs.functs.put(fm.getKey(), offset);
          offs.functLen++;
          offset++;
          writtenFuncts.add(fm.getKey());
        }
      }
    }

    return retList;
  }

}