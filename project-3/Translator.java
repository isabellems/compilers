//
// Generated by JTB 1.3.2 DIT@UoA patched
//

import syntaxtree.*;
import java.util.*;
import visitor.GJDepthFirst;
import java.io.PrintWriter;
import java.io.File;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class Translator extends GJDepthFirst<ReturnValue, ReturnValue> {

   //
   // User-generated visitor methods below
   //

  public static Map<String, ClassInfo> ClassTable;
  public static FunctionInfo MainTable;
  public static VTable vtable;
  public static String fileName;
  public static Map<String, String> types;
  public static PrintWriter printWriter;
  public static Queue<ReturnValue> queue;
  public static Integer reg;
  public static Integer ifLabel;
  public static Integer loopLabel;
  public static Integer oobLabel;
  public static Integer andLabel;
  public static Integer orLabel;
  public static Integer arrLabel;

  public Translator(String _fileName, Map<String, ClassInfo> _classTable, FunctionInfo _mainTable, VTable _vTable) throws Exception{
    types = new HashMap<String, String>();
    queue = new LinkedList<ReturnValue>();
    ClassTable = _classTable;
    MainTable = _mainTable;
    vtable = _vTable;
    reg = 0;
    ifLabel = 0;
    loopLabel = 0;
    oobLabel = 0;
    andLabel = 0;
    orLabel = 0;
    arrLabel = 0;
    types.put("boolean", "i1");
    types.put("char", "i8");
    types.put("pointer", "i8*");
    types.put("int", "i32");
    types.put("int[]", "i32*");
    types.put("int*", "i32*");

    int index = _fileName.lastIndexOf("/");
    if(index != -1){
      _fileName = _fileName.substring(index);
    }
    index = _fileName.lastIndexOf(".");
    if(index != -1){
      fileName = _fileName.substring(0, index);
    }

    File file = new File("./" + fileName + ".ll");
    printWriter = new PrintWriter(file);
  }

  public ReturnValue getTypeM(String cName, String member){
    ClassInfo cinfo = ClassTable.get(cName);
    ReturnValue _ret = new ReturnValue();
    if(cinfo.members.containsKey(member)){
      _ret.type = cinfo.members.get(member);
      _ret.value = cName;
    }
    else{
      String parent = cinfo.parent;
      while(parent != null){
        cinfo = ClassTable.get(parent);
        if(cinfo.members.containsKey(member)){
          _ret.type = cinfo.members.get(member);
          _ret.value = parent;
          break;
        }
        parent = cinfo.parent;
      }
    }
    return _ret;
  }

  public ReturnValue getTypeF(String cName, String member){

    ClassInfo cinfo = ClassTable.get(cName);
    ReturnValue _ret = new ReturnValue();
    _ret.typeValue = new FunctionInfo();
    if(cinfo.functions.containsKey(member)){
      _ret.typeValue = cinfo.functions.get(member);
      _ret.value = cName;
    }
    else{
      String parent = cinfo.parent;
      while(parent != null){
        cinfo = ClassTable.get(parent);
        if(cinfo.functions.containsKey(member)){
          _ret.typeValue = cinfo.functions.get(member);
          _ret.value = parent;
          break;
        }
        parent = cinfo.parent;
      }
    }
    return _ret;
  }

  public String getLLType(String type){
    if (type.equals("boolean") || type.equals("char") || type.equals("int") || type.equals("int[]") || type.equals("int*")){
      return types.get(type);
    }
    else{
      return types.get("pointer");
    }
  }

  public void emit(String str) throws Exception{
    try{
      printWriter.print(str);

    }
    catch(Exception ex){
      ex.printStackTrace();
    }
  }

  public void functionDef(FunctionInfo fun) throws Exception {
    emit("i8* bitcast (" + getLLType(fun.type) + " (" + getLLType("pointer"));

    Iterator<Map.Entry<String, String>> args = fun.arguments.entrySet().iterator();

    while (args.hasNext()) {
        Map.Entry<String, String> argument = args.next();
        emit("," + getLLType(argument.getValue()));
    }

    emit(")* @" + fun.owner + "." + fun.name + " to " + getLLType("pointer") + ")");

  }

  public String getReg(){
    String _ret = "%_" + String.valueOf(reg);
    reg++;
    return _ret;
  }

  public String getLabel(String state) {
    String _ret = null;
    if(state.equals("if")){
      _ret = "if" + String.valueOf(ifLabel);
      ifLabel++;
    }
    else if(state.equals("while")){
      _ret = "loop" + String.valueOf(loopLabel);
      loopLabel++;
    }
    else if(state.equals("and")){
      _ret = "andclause" + String.valueOf(andLabel);
      andLabel++;
    }
    else if(state.equals("arr")){
      _ret = "arr_alloc" + String.valueOf(arrLabel);
      arrLabel++;
    }
    else{
      _ret = "oob" + String.valueOf(oobLabel);
      oobLabel++;
    }
    return _ret;
  }

  public void resetReg() {
    reg = 3;
  }

  public void resetLabels() {
    ifLabel = 0;
    loopLabel = 0;
    oobLabel = 0;
    andLabel = 0;
    arrLabel = 0;
  }

  public void helperMethods() throws Exception{
    emit("\n\ndeclare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\ndeclare void @exit(i32)\n\n");
    emit("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n");
    emit("define void @print_int(i32 %i) {\n");
    emit("\t%_str = bitcast [4 x i8]* @_cint to i8*\n");
    emit("\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n");
    emit("\tret void\n}\n\n");
    emit("define void @throw_oob() {\n");
    emit("\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n");
    emit("\tcall i32 (i8*, ...) @printf(i8* %_str)\n");
    emit("\tcall void @exit(i32 1)\n");
    emit("\tret void\n}\n\n");

  }

  public void v_Table(String className) throws Exception{
    List<FunctionInfo> functs = vtable.getFunctionArray(className);
    int len = functs.size();
    emit("@." + className + "_vtable = global [" + len + " x " + getLLType("pointer") + "] [" );
    if(len > 0){
      int j = 0;
      FunctionInfo fun = functs.get(j);
      functionDef(fun);
      for(int i = 1; i < len; i++){
        emit(",");
        fun = functs.get(i);
        functionDef(fun);
      }
    }

    emit("]\n");
  }

   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
   public ReturnValue visit(Goal n, ReturnValue argu) throws Exception {
      ReturnValue _ret=null;

      emit("@." + MainTable.owner + "_vtable = global [0 x i8*] []\n" );
      Iterator<Map.Entry<String,ClassInfo>> classes = ClassTable.entrySet().iterator();
      Map.Entry<String,ClassInfo> _cl = classes.next(); //skip main class
      while (classes.hasNext()) {
          _cl = classes.next();
          v_Table(_cl.getKey());
      }
      helperMethods();
      argu = new ReturnValue();
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      
      printWriter.close(); 
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> "public"
    * f4 -> "static"
    * f5 -> "void"
    * f6 -> "main"
    * f7 -> "("
    * f8 -> "String"
    * f9 -> "["
    * f10 -> "]"
    * f11 -> Identifier()
    * f12 -> ")"
    * f13 -> "{"
    * f14 -> ( VarDeclaration() )*
    * f15 -> ( Statement() )*
    * f16 -> "}"
    * f17 -> "}"
    */
   public ReturnValue visit(MainClass n, ReturnValue argu) throws Exception {
      ReturnValue _ret = null;
      FunctionInfo finfo = MainTable;
      argu.typeValue = MainTable;
      emit("define i32 @main() {\n");
      n.f1.accept(this, argu);
      n.f11.accept(this, argu);
      n.f13.accept(this, argu);
      n.f14.accept(this, argu);
      if(finfo.identifiers.size() != 0){
        Iterator<Map.Entry<String,String>> identifiers = finfo.identifiers.entrySet().iterator();
        while (identifiers.hasNext()) {
            Map.Entry<String,String> id = identifiers.next();
            emit("\t%" + id.getKey() + " = alloca " + getLLType(id.getValue()) + "\n");
        }
        emit("\n");
      }
      n.f15.accept(this, argu);
      n.f16.accept(this, argu);
      n.f17.accept(this, argu);
      emit("\n\tret i32 0\n");
      emit("}\n\n");
      resetReg();
      return _ret;
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public ReturnValue visit(TypeDeclaration n, ReturnValue argu) throws Exception {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public ReturnValue visit(ClassDeclaration n, ReturnValue argu) throws Exception {
      ReturnValue _ret=null;
      n.f0.accept(this, argu);
      String className = n.f1.accept(this, argu).value;
      argu.value = className;
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "extends"
    * f3 -> Identifier()
    * f4 -> "{"
    * f5 -> ( VarDeclaration() )*
    * f6 -> ( MethodDeclaration() )*
    * f7 -> "}"
    */
   public ReturnValue visit(ClassExtendsDeclaration n, ReturnValue argu) throws Exception {
      ReturnValue _ret=null;
      n.f0.accept(this, argu);
      String className = n.f1.accept(this, argu).value;
      argu.value = className;
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      n.f4.accept(this, argu);
      n.f5.accept(this, argu);
      n.f6.accept(this, argu);
      n.f7.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public ReturnValue visit(VarDeclaration n, ReturnValue argu) throws Exception {
      ReturnValue _ret = null;
      String type = n.f0.accept(this, argu).value;
      String name = n.f1.accept(this, argu).value;
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "public"
    * f1 -> Type()
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( FormalParameterList() )?
    * f5 -> ")"
    * f6 -> "{"
    * f7 -> ( VarDeclaration() )*
    * f8 -> ( Statement() )*
    * f9 -> "return"
    * f10 -> Expression()
    * f11 -> ";"
    * f12 -> "}"
    */
   public ReturnValue visit(MethodDeclaration n, ReturnValue argu) throws Exception {
      ReturnValue _ret = null;
      String type = n.f1.accept(this, argu).value;
      String name = n.f2.accept(this, argu).value;
      emit("define " + getLLType(type) + " @" + argu.value + "." + name + "(i8* %this");
      ClassInfo cinfo = ClassTable.get(argu.value);
      FunctionInfo finfo = cinfo.functions.get(name);
      if(finfo.arguments.size() == 0){
        emit(") {\n");
      }
      else {
        Iterator<Map.Entry<String,String>> arguments = finfo.arguments.entrySet().iterator();
        while (arguments.hasNext()) {
            Map.Entry<String,String> arg = arguments.next();
            emit(", " + getLLType(arg.getValue()) + " %." + arg.getKey());
        }
        emit(") {\n");

        arguments = finfo.arguments.entrySet().iterator();
        while (arguments.hasNext()) {
            Map.Entry<String,String> arg = arguments.next();
            emit("\t%" + arg.getKey() + " = alloca " + getLLType(arg.getValue()) + "\n");
            emit("\tstore " + getLLType(arg.getValue()) + " %." + arg.getKey() + ",  " + getLLType(arg.getValue()) + "* %" + arg.getKey() + "\n");
        }
        emit("\n");
      }
      if(finfo.identifiers.size() != 0){
        Iterator<Map.Entry<String,String>> identifiers = finfo.identifiers.entrySet().iterator();
        while (identifiers.hasNext()) {
            Map.Entry<String,String> id = identifiers.next();
            emit("\t%" + id.getKey() + " = alloca " + getLLType(id.getValue()) + "\n");
        }
        emit("\n");
      }
      argu.typeValue = finfo;

      n.f7.accept(this, argu);
      n.f8.accept(this, argu);
      ReturnValue exp = n.f10.accept(this, argu);
      emit("\tret " + getLLType(exp.type) + " " + exp.reg + "\n");
      emit("}\n\n");
      resetReg();
      resetLabels();
      return _ret;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
   public ReturnValue visit(FormalParameterList n, ReturnValue argu) throws Exception {
      ReturnValue _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public ReturnValue visit(FormalParameter n, ReturnValue argu) throws Exception {
      ReturnValue _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ( FormalParameterTerm() )*
    */
   public ReturnValue visit(FormalParameterTail n, ReturnValue argu) throws Exception {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public ReturnValue visit(FormalParameterTerm n, ReturnValue argu) throws Exception {
      ReturnValue _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
   public ReturnValue visit(Type n, ReturnValue argu) throws Exception {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public ReturnValue visit(ArrayType n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      _ret.value = "int[]";
      return _ret;
   }

   /**
    * f0 -> "boolean"
    */
   public ReturnValue visit(BooleanType n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      _ret.value = "boolean";
      return _ret;
   }

   /**
    * f0 -> "int"
    */
   public ReturnValue visit(IntegerType n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      _ret.value = "int";
      return _ret;
   }

   /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
   public ReturnValue visit(Statement n, ReturnValue argu) throws Exception {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
   public ReturnValue visit(Block n, ReturnValue argu) throws Exception {
      ReturnValue _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public ReturnValue visit(AssignmentStatement n, ReturnValue argu) throws Exception { //TODO MEMBERS
      ReturnValue _ret = new ReturnValue();
      ReturnValue id = n.f0.accept(this, argu);
      ReturnValue exp = n.f2.accept(this, argu);
      FunctionInfo finfo = (FunctionInfo) argu.typeValue;
      ClassInfo cinfo = ClassTable.get(finfo.owner);
      if(finfo.arguments.containsKey(id.value)){
        id.type = finfo.arguments.get(id.value);
      }
      else if(finfo.identifiers.containsKey(id.value)){
        id.type = finfo.identifiers.get(id.value);
      }
      else{
        ReturnValue retType = getTypeM(cinfo.name, id.value); 
        cinfo = ClassTable.get(retType.value);
        id.type = retType.type;        
        Integer offset = vtable.getMember(id.value, retType.value);
        String reg1 = getReg();
        emit("\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + new Integer(offset.intValue() + 8) + "\n");
        id.reg = getReg();
        emit("\t" + id.reg + " = bitcast i8* " + reg1 + " to " + getLLType(id.type) + "*\n");
      }

      emit("\tstore " + getLLType(id.type) + " " + exp.reg + ", " + getLLType(id.type) + "* " + id.reg + "\n");
      emit("\n");
      n.f3.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
   public ReturnValue visit(ArrayAssignmentStatement n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FunctionInfo finfo = (FunctionInfo) argu.typeValue;
      ClassInfo cinfo = ClassTable.get(finfo.owner);
      ReturnValue id = n.f0.accept(this, argu);
      String reg1 = getReg();
      String type = null; 
      if(finfo.arguments.containsKey(id.value)) { 
        type = finfo.arguments.get(id.value);
        emit("\t" + reg1 + " = load " + getLLType(type) + ", " + getLLType(type) + "* " + id.reg + "\n");
      }
      else if(finfo.identifiers.containsKey(id.value)){
        type = finfo.identifiers.get(id.value);
        emit("\t" + reg1 + " = load " + getLLType(type) + ", " + getLLType(type) + "* " + id.reg + "\n");
      }
      else{
        ReturnValue retType = getTypeM(cinfo.name, id.value);
        type = retType.type;
        Integer offset = vtable.getMember(id.value, retType.value);
        emit("\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + new Integer(offset.intValue() + 8) + "\n");
        String reg2 = getReg();
        emit("\t" + reg2 + " = bitcast i8* " + reg1 + " to " + getLLType(type) + "*\n");
        reg1 = getReg();
        emit("\t" + reg1 + " = load " + getLLType(type) + ", " + getLLType(type) + "* " + reg2 + "\n");
      }
      ReturnValue exp1 = n.f2.accept(this, argu);
      String reg3 = getReg();
      emit("\t" + reg3 + " = load i32, i32 *" + reg1 + "\n"); 
      String reg4 = getReg();
      emit("\t" + reg4 + " = icmp ult i32 " + exp1.reg + ", " + reg3 + "\n");
      String oob0 = getLabel("oob");
      String oob1 = getLabel("oob");
      String oob2 = getLabel("oob");
      emit("\tbr i1 " + reg4 + ", label %" + oob0 + ", label %" + oob1 + "\n");
      emit("\n\n" + oob0 + ":\n");
      reg4 = getReg();
      emit("\t" + reg4 + " = add i32 " + exp1.reg + ", " + 1 + "\n");
      String reg5 = getReg();
      emit("\t" + reg5 + " = getelementptr i32, i32* " + reg1 + ", " + getLLType(exp1.type) + " " + reg4 + "\n");
      ReturnValue exp2 = n.f5.accept(this, argu);
      emit("\tstore i32 " + exp2.reg +  ", i32* " +  reg5 + "\n");
      emit("\tbr label %" + oob2 + "\n");
      emit("\n\n" + oob1 + ":\n");
      emit("\tcall void @throw_oob()\n");
      emit("\tbr label %" + oob2 + "\n");
      emit("\n\n" + oob2 + ":\n");

      return _ret;
   }

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
   public ReturnValue visit(IfStatement n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp = n.f2.accept(this, argu);
      String label1 = getLabel("if");
      String label2 = getLabel("if");
      String label3 = getLabel("if");
      emit("\tbr i1 " + exp.reg + ", label %" + label1 + ", label %" + label2 + "\n\n");
      emit(label1 + ":\n");
      n.f4.accept(this, argu);
      emit("\n\tbr label %" + label3 + "\n\n");
      emit(label2 + ":\n");
      n.f6.accept(this, argu);
      emit("\n\tbr label %" + label3 + "\n\n");
      emit(label3 + ":\n");
      return _ret;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public ReturnValue visit(WhileStatement n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      String label0 = getLabel("while");
      emit("\tbr label %" + label0 + "\n\n");
      emit(label0 + ":\n");
      ReturnValue exp = n.f2.accept(this, argu);
      String label1 = getLabel("while");
      String label2 = getLabel("while");
      emit("\tbr i1 " +  exp.reg + ", label %" + label1 + ", label %" + label2 +"\n"); 
      emit(label1 + ":\n");
      n.f4.accept(this, argu);
      emit("\tbr label %" + label0 + "\n");
      emit(label2 + ":\n");
      return _ret;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public ReturnValue visit(PrintStatement n, ReturnValue argu) throws Exception {
      ReturnValue _ret = n.f2.accept(this, argu);
      emit("\tcall void (i32) @print_int(i32 " + _ret.reg + ")\n");
      return _ret;
   }

   /**
    * f0 -> AndExpression()
    *       | CompareExpression()
    *       | PlusExpression()
    *       | MinusExpression()
    *       | TimesExpression()
    *       | ArrayLookup()
    *       | ArrayLength()
    *       | MessageSend()
    *       | Clause()
    */
   public ReturnValue visit(Expression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
   public ReturnValue visit(AndExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp1 = n.f0.accept(this, argu);
      String label0 = getLabel("and");
      String label1 = getLabel("and");
      String label2 = getLabel("and");
      String label3 = getLabel("and");
      emit("\tbr label %" + label0 + "\n");
      emit("\n" + label0 + ":\n");
      emit("\tbr i1 " + exp1.reg + ", label %" + label1 + ", label %" + label3 + "\n");
      emit("\n" + label1 + ":\n");
      ReturnValue exp2 = n.f2.accept(this, argu);
      emit("\tbr label %" + label2 + "\n");
      emit("\n" + label2 + ":\n");
      emit("\tbr label %" + label3 + "\n");
      emit("\n" + label3 + ":\n");
      _ret.reg = getReg();
      emit("\t" + _ret.reg + " = phi i1 [ 0, %" + label0 + " ], [ " + exp2.reg + ", %" + label2 + " ]\n");
      _ret.type = "boolean";
      _ret.kind = "and";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   public ReturnValue visit(CompareExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp1 = n.f0.accept(this, argu);
      ReturnValue exp2 = n.f2.accept(this, argu);
      String reg = getReg();
      emit("\t" + reg + " = icmp slt "); 
      emit(getLLType(exp1.type) + " " + exp1.reg + ", " + exp2.reg + "\n");
      _ret.type = "boolean";
      _ret.kind = "compare";
      _ret.reg = reg;
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public ReturnValue visit(PlusExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp1 = n.f0.accept(this, argu);
      ReturnValue exp2 = n.f2.accept(this, argu);
      String reg = getReg();
      emit("\t" + reg + " = add "); 
      emit(getLLType(exp1.type) + " " + exp1.reg + ", " + exp2.reg + "\n");
      _ret.type = exp1.type;
      _ret.reg = reg;
      _ret.kind = "add";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public ReturnValue visit(MinusExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp1 = n.f0.accept(this, argu);
      ReturnValue exp2 = n.f2.accept(this, argu);
      String reg = getReg();
      emit("\t" + reg + " = sub "); 
      emit(getLLType(exp1.type) + " " + exp1.reg + ", " + exp2.reg + "\n");
      _ret.type = exp1.type;
      _ret.reg = reg;
      _ret.kind = "sub";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public ReturnValue visit(TimesExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp1 = n.f0.accept(this, argu);
      ReturnValue exp2 = n.f2.accept(this, argu);
      String reg = getReg();
      emit("\t" + reg + " = mul ");
      emit(getLLType(exp1.type) + " " + exp1.reg + ", " + exp2.reg + "\n"); 
      _ret.type = exp1.type;
      _ret.reg = reg;
      _ret.kind = "mul";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public ReturnValue visit(ArrayLookup n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp1 = n.f0.accept(this, argu);
      ReturnValue exp2 = n.f2.accept(this, argu);
      String reg1 = getReg();
      emit("\t" + reg1 + " = load i32, i32 *" + exp1.reg + "\n");
      String reg2 = getReg();
      emit("\t" + reg2 + " = icmp ult i32 " + exp2.reg + ", " + reg1 + "\n");
      String oob0 = getLabel("oob");
      String oob1 = getLabel("oob");
      String oob2 = getLabel("oob");
      emit("\tbr i1 " + reg2 + ", label %" + oob0 + ", label %" + oob1 + "\n");
      emit("\n" + oob0 + ":\n");
      String reg3 = getReg();
      emit("\t" + reg3 + " = add i32 " + exp2.reg + ", 1\n");
      reg1 = getReg();
      emit("\t" + reg1 + " = getelementptr i32 , " + getLLType(exp1.type) + " " + exp1.reg + ", i32 " + reg3 + "\n");
      reg3 = getReg();
      emit("\t" + reg3 + " = load i32, i32* " + reg1 + "\n");
      emit("\tbr label %" + oob2 + "\n");
      emit("\n" + oob1 + ":\n");
      emit("\tcall void @throw_oob()\n");
      emit("\tbr label %" + oob2 + "\n");
      emit("\n" + oob2 + ":\n");
      _ret.reg = reg3;
      _ret.type = "int";
      _ret.kind = "lookup";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public ReturnValue visit(ArrayLength n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp = n.f0.accept(this, argu);
      String reg = getReg();
      emit("\t" + reg + " = load i32, i32 *" + exp.reg +"\n");
      _ret.reg = reg;
      _ret.type = "int";
      _ret.kind = "length";
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
   public ReturnValue visit(MessageSend n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp = n.f0.accept(this, argu);
      ClassInfo cinfo = ClassTable.get(exp.type);
      String reg1 = getReg();
      String type = exp.type;
      argu.extReg = exp.reg;
      emit("\t" + reg1 + " = bitcast i8* " + exp.reg + " to i8***\n");
      String reg2 = getReg();
      emit("\t" + reg2 + " = load i8**, i8*** " + reg1 + "\n");
      exp = n.f2.accept(this, argu);
      ReturnValue retValue = getTypeF(cinfo.name, exp.value);
      FunctionInfo funct = (FunctionInfo) retValue.typeValue;
      Integer offset = vtable.getFunction(exp.value, cinfo.name);
      String reg3 = getReg();
      emit("\t" + reg3 + " = getelementptr i8*, i8** " + reg2 + ", i32 " + offset + "\n");
      String reg4 = getReg();
      emit("\t" + reg4 + " = load i8*, i8** " + reg3 + "\n");
      String reg5 = getReg();
      emit("\t" + reg5 + " = bitcast i8* " + reg4 + " to " + getLLType(funct.type) + " (i8*");

      Iterator<Map.Entry<String,String>> argus = funct.arguments.entrySet().iterator();
      while(argus.hasNext()){
        Map.Entry<String, String> arg = argus.next();
        emit(", " + getLLType(arg.getValue()));
      }
      emit(")*\n");
      if(n.f4.present()){
        argu.type = funct.type;
        argu.reg = reg5;
        exp = n.f4.accept(this, argu);
       _ret.reg = exp.reg;
      }
      else{
        _ret.reg = getReg();
        emit("\t" + _ret.reg + " = call " + getLLType(funct.type) + " " + reg5 + "(i8* " + argu.extReg + ")\n");
      }
      emit("\n");
      _ret.type = funct.type;
      _ret.kind = "message";
      return _ret;
   }

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
   public ReturnValue visit(ExpressionList n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      String argReg = argu.extReg;
      ReturnValue exp = n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      String reg = getReg();
      String type = new String();
      emit("\t" + reg + " = call " + getLLType(argu.type) + " " + argu.reg + "(i8* " + argReg);
      emit(", " + getLLType(exp.type) + " " + exp.reg);
      while (!queue.isEmpty()) {
        ReturnValue expT = queue.remove();
        emit(", " + getLLType(expT.type) + " " + expT.reg);
      }
      emit(")\n");
      _ret.reg = reg;
      return _ret;
   }

   /**
    * f0 -> ( ExpressionTerm() )*
    */
   public ReturnValue visit(ExpressionTail n, ReturnValue argu) throws Exception {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public ReturnValue visit(ExpressionTerm n, ReturnValue argu) throws Exception {
      ReturnValue _ret = n.f1.accept(this, argu);
      queue.add(_ret);
      // String type = new String();
      // emit(", " + getLLType(exp.type) + " " + exp.reg);
      return _ret;
   }

   /**
    * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
   public ReturnValue visit(Clause n, ReturnValue argu) throws Exception {
      return n.f0.accept(this, argu);
   }

   /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | BracketExpression()
    */
   public ReturnValue visit(PrimaryExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      FunctionInfo finfo = (FunctionInfo) argu.typeValue;
      ClassInfo cinfo = ClassTable.get(finfo.owner);
      if(_ret.kind.equals("identifier")){
        String reg1 = getReg();
        if(finfo.arguments.containsKey(_ret.value)) { 
          _ret.type = finfo.arguments.get(_ret.value);
          emit("\t" + reg1 + " = load " + getLLType(_ret.type) + ", " + getLLType(_ret.type) + "* " + _ret.reg + "\n");
        }
        else if(finfo.identifiers.containsKey(_ret.value)){
          _ret.type = finfo.identifiers.get(_ret.value);
          emit("\t" + reg1 + " = load " + getLLType(_ret.type) + ", " + getLLType(_ret.type) + "* " + _ret.reg + "\n");
        }
        else{
          ReturnValue retType = getTypeM(cinfo.name, _ret.value);
          _ret.type = retType.type;
          Integer offset = vtable.getMember(_ret.value, retType.value);
          emit("\t" + reg1 + " = getelementptr i8, i8* %this, i32 " + new Integer(offset.intValue() + 8) + "\n");
          String reg2 = getReg();
          emit("\t" + reg2 + " = bitcast i8* " + reg1 + " to " + getLLType(_ret.type) + "*\n");
          reg1 = reg2;
          reg1 = getReg();
          emit("\t" + reg1 + " = load " + getLLType(_ret.type) + ", " + getLLType(_ret.type) + "* " + reg2 + "\n");
        }
         _ret.reg = reg1;
      }
      else if(_ret.kind.equals("this")){
          cinfo = ClassTable.get(finfo.owner);
          _ret.type = finfo.owner;
      }
      else if(_ret.kind.equals("constant")){
        _ret.reg = _ret.value;
      }
      return _ret;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public ReturnValue visit(IntegerLiteral n, ReturnValue argu) throws Exception {
       ReturnValue _ret = new ReturnValue();
      _ret.value = n.f0.toString();
      _ret.type = "int";
      _ret.kind = "constant";
      return _ret;
   }

   /**
    * f0 -> "true"
    */
   public ReturnValue visit(TrueLiteral n, ReturnValue argu) throws Exception {
       ReturnValue _ret = new ReturnValue();
      _ret.value = "1";
      _ret.type = "boolean";
      _ret.kind = "constant";
      return _ret;
   }

   /**
    * f0 -> "false"
    */
   public ReturnValue visit(FalseLiteral n, ReturnValue argu) throws Exception {
       ReturnValue _ret = new ReturnValue();
      _ret.value = "0";
      _ret.type = "boolean";
      _ret.kind = "constant";
      return _ret;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public ReturnValue visit(Identifier n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      _ret.value = n.f0.toString();
      _ret.kind = "identifier";
      _ret.type = "identifier";
      _ret.reg = "%" + _ret.value;
      return _ret;
   }

   /**
    * f0 -> "this"
    */
   public ReturnValue visit(ThisExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      _ret.kind = "this";
      _ret.reg = "%this";
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public ReturnValue visit(ArrayAllocationExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp = n.f3.accept(this, argu);
      String reg1 = getReg(); 
      emit("\t" + reg1 + " = icmp slt i32 " + exp.reg + ", 0\n");
      String label0 = getLabel("arr");
      String label1 = getLabel("arr");
      emit("\tbr i1 " + reg1 + ", label %" + label0 + ", label %" + label1 + "\n");
      emit("\n" + label0 + ":\n");
      emit("\tcall void @throw_oob()\n");
      emit("\tbr label %" + label1 + "\n");
      emit("\n" + label1 + ":\n");
      reg1 = getReg();
      emit("\t" + reg1 + " = add i32 " + exp.reg + ", 1\n");
      String reg2 = getReg();
      emit("\t" + reg2 + " = call i8* @calloc(i32 4, i32 " + reg1 + ")\n");
      String reg3 = getReg();
      emit("\t" + reg3 + " = bitcast i8* " + reg2 + " to i32*\n");
      emit("\tstore i32 " + exp.reg + ", i32* " + reg3 + "\n");
      emit("\n");
      _ret.reg = reg3;
      _ret.type = "int*";
      _ret.kind = "new[]";
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public ReturnValue visit(AllocationExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      String reg1 = new String();
      ReturnValue id = n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      n.f3.accept(this, argu);
      int size = vtable.getSize(id.value) + 8;
      ClassInfo cinfo = ClassTable.get(id.value);
      reg1 = getReg();
      emit("\t" + reg1 + " = call i8* @calloc(i32 1, i32 " + size + ")\n");
      String reg2 = getReg();
      emit("\t" + reg2 + " = bitcast i8* " + reg1 + " to i8***\n");
      String reg3 = getReg();
      String functs = "[" + vtable.getFunctionsLen(cinfo.name) + " x " + "i8*]";
      emit("\t" + reg3 + " = getelementptr " + functs + ", " + functs + "* @." + id.value + "_vtable,  i32 0, i32 0 \n");
      emit("\tstore i8** " + reg3 + ", i8*** " + reg2 + "\n");
      emit("\n");
      _ret.reg = reg1;
      _ret.kind = "new";
      _ret.type = id.value;
      return _ret;
   }

   /**
    * f0 -> "!"
    * f1 -> Clause()
    */
   public ReturnValue visit(NotExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp = n.f1.accept(this, argu);
      String reg = getReg();
      emit("\t" + reg + " = xor " + getLLType(exp.type) + " 1, " + exp.reg + "\n");
      _ret.reg = reg;
      _ret.kind = "not";
      _ret.type = "boolean";
      return _ret;
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public ReturnValue visit(BracketExpression n, ReturnValue argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue exp = n.f1.accept(this, argu);
      _ret.value = exp.value;
      _ret.reg = exp.reg;
      _ret.type = exp.type;
      _ret.kind = "bracket";
      return _ret;
   }

}
