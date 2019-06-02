import syntaxtree.*;
import java.util.*;
import visitor.GJDepthFirst;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class MyTypeChecker extends GJDepthFirst<ReturnValue, FunctionInfo> {
    
  public static Map<String, ClassInfo> ClassTable;
  public static FunctionInfo MainTable;
  public static ArrayList<String> primitives;

  public MyTypeChecker(Map<String, ClassInfo> _classTable, FunctionInfo _mainTable){
      ClassTable = _classTable;
      MainTable = _mainTable;
      primitives = new ArrayList<String>();
      primitives.add("int");
      primitives.add("int[]");
      primitives.add("boolean");
  }

  public ReturnValue idExists(String name, FunctionInfo argu) throws Exception{
    ReturnValue _ret = new ReturnValue();
    FieldInfo field = new FieldInfo();
    field.name = name;
    if(name.equals("this")){
      field.name = name;
      field.type = argu.owner;
      _ret.typeValue = field;
      return _ret;
    }
    if(argu.name.equals("main")){ //if argu == main() 
      if(MainTable.arguments.containsKey(field.name)){
        field.type = MainTable.arguments.get(field.name);
      }
      else if(MainTable.identifiers.containsKey(field.name)){
        field.type = MainTable.identifiers.get(field.name);
      } 
      else{
        throw new Exception("error: cannot find symbol " + field.name);
      }
    }
    else{
      ClassInfo ownerInfo = ClassTable.get(argu.owner);
      if(argu.arguments.containsKey(field.name)){
        field.type = argu.arguments.get(field.name);
      }
      else if(argu.identifiers.containsKey(field.name)){
        field.type = argu.identifiers.get(field.name);
      }
      else if(ownerInfo.members.containsKey(field.name)){
        field.type = ownerInfo.members.get(field.name);
      }
      else{
        String parent = ownerInfo.parent;
        Boolean exists = false;
        while(parent != null){
          ClassInfo parInfo = ClassTable.get(parent);
          if(parInfo.members.containsKey(field.name)){
            field.type = parInfo.members.get(field.name);
            exists = true;
            break;
          }
          parent = parInfo.parent;
        }
        if(!exists){
          throw new Exception("error: cannot find symbol " + name);
        }
      }
    }
      _ret.typeValue = field;
      return _ret;
  }

  public FunctionInfo functExists(String name, String owner) throws Exception{
    if(!ClassTable.containsKey(owner)){
        throw new Exception("error: cannot find symbol \n\tsymbol: class " + owner);
    }
    ClassInfo ownerInfo = ClassTable.get(owner);
    FunctionInfo funct = null;
    if(ownerInfo.functions.containsKey(name)){
      funct = ownerInfo.functions.get(name);
    }
    else{
      String parent = ownerInfo.parent;
      Boolean exists = false;
      while(parent != null){
        ClassInfo parInfo = ClassTable.get(parent);
        if(parInfo.functions.containsKey(name)){
          funct = parInfo.functions.get(name);
          exists = true;
          break;
        }
        parent = parInfo.parent;
      }
      if(!exists){
        throw new Exception("error: cannot find symbol \n\tsymbol: method " + name);
      }
    }
    return funct;
  }

  public Boolean isDerived(String name, String parent){
    if(primitives.contains(name) || primitives.contains(parent)){
      return false;
    }
    ClassInfo subClass = ClassTable.get(name);
    String currParent = subClass.parent;
    while(currParent != null){
      if(currParent.equals(parent)){
        return true;
      }
      subClass = ClassTable.get(currParent);
      currParent = subClass.parent;
    }
    return false;
  }

   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
   public ReturnValue visit(Goal n, FunctionInfo argu) throws Exception {
      ReturnValue _ret=null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      n.f2.accept(this, argu);
      System.out.println("Program semantically correct.");
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
   public ReturnValue visit(MainClass n, FunctionInfo argu) throws Exception {
      ReturnValue _ret=null;
      n.f14.accept(this, argu);
      n.f15.accept(this, MainTable);
      return _ret;
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public ReturnValue visit(TypeDeclaration n, FunctionInfo argu) throws Exception {
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
   public ReturnValue visit(ClassDeclaration n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      FunctionInfo finfo = new FunctionInfo();
      FieldInfo field = new FieldInfo();
      _ret = n.f1.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      finfo.owner = field.name;
      if(n.f3.present()){
        n.f3.accept(this, argu);
      }
      if(n.f4.present()){
        n.f4.accept(this, finfo);
      }
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
   public ReturnValue visit(ClassExtendsDeclaration n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      FunctionInfo finfo = new FunctionInfo();
      FieldInfo field = new FieldInfo();
      _ret = n.f1.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      finfo.owner = field.name;
      if(n.f5.present()){
        n.f5.accept(this, finfo);
      }
      if(n.f6.present()){
        n.f6.accept(this, finfo);
      }
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public ReturnValue visit(VarDeclaration n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      ReturnValue val = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) val.typeValue;
      if(!primitives.contains(field.name) && !ClassTable.containsKey(field.name)){
        throw new Exception("error: cannot find symbol " + field.name);
      }
      val = n.f1.accept(this, argu);
      field = (FieldInfo) val.typeValue;
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
   public ReturnValue visit(MethodDeclaration n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue val = n.f1.accept(this, argu);
      FieldInfo field = (FieldInfo) val.typeValue;
      ClassInfo owner;
      String type = field.name;
      if(!primitives.contains(field.name) && !ClassTable.containsKey(field.name)){
        throw new Exception("error: cannot find symbol " + field.name);
      }
      val = n.f2.accept(this, argu);
      field = (FieldInfo) val.typeValue;
      owner = ClassTable.get(argu.owner);
      argu = owner.functions.get(field.name);
      n.f4.accept(this, argu);
      n.f7.accept(this, argu);
      n.f8.accept(this, argu);
      val = n.f10.accept(this, argu);
      field = (FieldInfo) val.typeValue;
      if(!field.type.equals(type) && !isDerived(field.type, type)){
        throw new Exception("error: incompatible types: " + field.type + " cannot be converted to " + type + "\n\t (return " + field.name + ")");
      }
      return _ret;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
   public ReturnValue visit(FormalParameterList n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      n.f0.accept(this, argu);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public ReturnValue visit(FormalParameter n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      ReturnValue val = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) val.typeValue;
      if(!primitives.contains(field.name) && !ClassTable.containsKey(field.name)){
        throw new Exception("error: cannot find symbol " + field.name);
      }
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ( FormalParameterTerm() )*
    */
   public ReturnValue visit(FormalParameterTail n, FunctionInfo argu) throws Exception {
      return n.f0.accept(this, argu);
   }

      /**
    * f0 -> ","
    * f1 -> FormalParameter()
    */
   public ReturnValue visit(FormalParameterTerm n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      _ret = n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ArrayType()
    *       | BooleanType()
    *       | IntegerType()
    *       | Identifier()
    */
   public ReturnValue visit(Type n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      _ret = n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public ReturnValue visit(ArrayType n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      field.name = n.f0.toString() + n.f1.toString() + n.f2.toString();
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> "boolean"
    */
   public ReturnValue visit(BooleanType n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      field.name = n.f0.toString();
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> "int"
    */
   public ReturnValue visit(IntegerType n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      field.name = n.f0.toString();
      _ret.typeValue = field;
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
   public ReturnValue visit(Statement n, FunctionInfo argu) throws Exception {
      ReturnValue _ret=null;
      n.f0.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "{"
    * f1 -> ( Statement() )*
    * f2 -> "}"
    */
   public ReturnValue visit(Block n, FunctionInfo argu) throws Exception {
      ReturnValue _ret=null;
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
   public ReturnValue visit(AssignmentStatement n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) _ret.typeValue;
      String name = field.name;
      String type;
      _ret = idExists(field.name, argu);
      field = (FieldInfo) _ret.typeValue;
      type = field.type;
      name = name + " = " ;
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      name = name + field.name;
      if(!type.equals(field.type) && !isDerived(field.type, type)){
        throw new Exception("error: incompatible types: " + field.type + " cannot be converted to " + type + "\n\t(" + name + ")");
      }
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
   public ReturnValue visit(ArrayAssignmentStatement n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) _ret.typeValue;
      String name = field.name;
      String badTypeStr = "";
      String corTypeStr = "";
      Boolean badType = false;
      _ret = idExists(field.name, argu);
      field = (FieldInfo) _ret.typeValue;
      if(!field.type.equals("int[]")){
        badType = true;
        badTypeStr = field.type;
        corTypeStr = "int[]";
      }
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      name = name + "["  + field.name + "] = "; 
      if(!field.type.equals("int") && !badType){
        badType = true;
        badTypeStr = field.type;
        corTypeStr = "int";
      }
      _ret = n.f5.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      name = name + field.name;
      if(!field.type.equals("int") && !badType){
        badType = true;
        badTypeStr = field.type;
        corTypeStr = "int";
      }
      if(badType){
        throw new Exception("error: incompatible types: " + badTypeStr + " cannot be converted to " + corTypeStr + "\n\t(" + name + ")");
      }
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
   public ReturnValue visit(IfStatement n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      FieldInfo field = null;
      String name = "if(";
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      name = name + field.name + ")";
      if(!field.type.equals("boolean")){
        throw new Exception("error: incompatible types: " + field.type + " cannot be converted to boolean " + "\n\t(" + name + ")");
      }
      n.f4.accept(this, argu);
      n.f6.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
   public ReturnValue visit(WhileStatement n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      FieldInfo field = null;
      String name = "while(";
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      name = name + field.name + ")";
      if(!field.type.equals("boolean")){
        throw new Exception("error: incompatible types: " + field.type + " cannot be converted to boolean " + "\n\t(" + name + ")");
      }
      n.f4.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
   public ReturnValue visit(PrintStatement n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      FieldInfo field = null;
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      if(!field.type.equals("int")){
        throw new Exception("error: incompatible types: " + field.type + " cannot be converted to int " + "\n\t(System.out.println(" + field.name + "))");
      }
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
   public ReturnValue visit(Expression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) _ret.typeValue;
      return _ret;
   }

   /**
    * f0 -> Clause()
    * f1 -> "&&"
    * f2 -> Clause()
    */
   public ReturnValue visit(AndExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) _ret.typeValue;
      Boolean badTypes = false;
      String type1, type2, name1 , name2;
      type1 = field.type;
      name1 = field.name;
      if(!field.type.equals("boolean")){
        badTypes = true;
      }
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      type2 = field.type;
      name2 = field.name;
      if(!field.type.equals("boolean")){
        badTypes = true;
      }
      field.name = name1 + " && " + name2;
      if(badTypes){
        throw new Exception("error: bad operand types for binary operator '&&' \n" + "first type: " + type1 + "\nsecond type: " + type2 + "\n\t(" + field.name + ")");
      }
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
   public ReturnValue visit(CompareExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      String type1, type2, name1, name2;
      FieldInfo field = (FieldInfo) _ret.typeValue;
      Boolean badTypes = false;
      if(!field.type.equals("int")){
        badTypes = true;
      }
      type1 = field.type;
      name1 = field.name;
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;

      if(!field.type.equals("int")){
       badTypes = true;
      }

      type2 = field.type;
      name2 = field.name;
      field.name = name1 + " < " + name2;
      if(badTypes){
        throw new Exception("error: bad operand types for binary operator '<' \n" + "first type: " + type1 + "\nsecond type: " + type2 + "\n\t(" + field.name + ")");
      }
      field.type = "boolean";
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
   public ReturnValue visit(PlusExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      String type1, type2, name1, name2;
      FieldInfo field = (FieldInfo) _ret.typeValue;
      Boolean badTypes = false;
      if(!field.type.equals("int")){
        badTypes = true;
      }
      type1 = field.type;
      name1 = field.name;
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;

      if(!field.type.equals("int")){
       badTypes = true;
      }
      type2 = field.type;
      name2 = field.name;
      field.name = name1 + " + " + name2;
      if(badTypes){
        throw new Exception("error: bad operand types for binary operator '+' \n" + "first type: " + type1 + "\nsecond type: " + type2 + "\n\t(" + field.name + ")");
      }
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
   public ReturnValue visit(MinusExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      String type1, type2, name1, name2;
      FieldInfo field = (FieldInfo) _ret.typeValue;
      Boolean badTypes = false;
      if(!field.type.equals("int")){
        badTypes = true;
      }
      type1 = field.type;
      name1 = field.name;
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;

      if(!field.type.equals("int")){
       badTypes = true;
      }
      type2 = field.type;
      name2 = field.name;
      field.name = name1 + " - " + name2;
      if(badTypes){
        throw new Exception("error: bad operand types for binary operator '-' \n" + "first type: " + type1 + "\nsecond type: " + type2 + "\n\t(" + field.name + ")");
      }
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
   public ReturnValue visit(TimesExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      String type1, type2, name1, name2;
      FieldInfo field = (FieldInfo) _ret.typeValue;
      Boolean badTypes = false;
      if(!field.type.equals("int")){
        badTypes = true;
      }
      type1 = field.type;
      name1 = field.name;
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;

      if(!field.type.equals("int")){
       badTypes = true;
      }
      type2 = field.type;
      name2 = field.name;
      field.name = name1 + " * " + name2;
      if(badTypes){
        throw new Exception("error: bad operand types for binary operator '*' \n" + "first type: " + type1 + "\nsecond type: " + type2 + "\n\t(" + field.name + ")");
      }
      _ret.typeValue = field;
      return _ret;
  }
   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
   public ReturnValue visit(ArrayLookup n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) _ret.typeValue;
      String name1, name2, type1, type2;
      Boolean badType1 = false;
      Boolean badType2 = false;
      name1 = field.name;
      type1 = field.type;
      if(!field.type.equals("int[]")){
        badType1 = true;
      }
      _ret = n.f2.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      if(!badType1 && !field.type.equals("int")){
        badType2 = true;
      }
     
      name2 = field.name;
      type2 = field.type;
      field.name = name1 + "[" + name2 + "]";
      if(badType1){
        throw new Exception("error: array required, but " + type1 + " found\n\t(" + field.name + ")");
      }
      else if(badType2){
        throw new Exception("error: incompatible types: " + type2 + " cannot be converted to int\n\t(" + field.name + ")");
      }
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
   public ReturnValue visit(ArrayLength n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) _ret.typeValue;
      field.name = field.name + ".length";
      if(!field.type.equals("int[]")){
        throw new Exception("error: array required, but " + field.type + " found\n\t(" + field.name + ".length)");
      }
      field.type = "int";
      _ret.typeValue = field;
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
   public ReturnValue visit(MessageSend n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      ReturnValue val = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) val.typeValue;
      FunctionInfo funct;
      String name = field.name;
      if(field.type.equals("int") || field.type.equals("boolean")){
        val = n.f2.accept(this, argu);
        field = (FieldInfo) val.typeValue;
        name = name + "." + field.name;
        throw new Exception("error: ';' expected \n\t(" + name + ")");
      }
      else{
        String type = field.type;
        val = n.f2.accept(this, argu);
        field = (FieldInfo) val.typeValue;
        funct = functExists(field.name, type);
        name = name + "." + field.name;
      }
      if(n.f4.present()){
        if (funct.arguments.size() == 0){
          throw new Exception("error: method " + funct.name + " cannot be applied to given types\n\t reason: actual and formal argument lists differ in length");
        }
       val = n.f4.accept(this, argu);
       FunctionInfo call = (FunctionInfo) val.typeValue;
       if(funct.arguments.size() != call.arguments.size()){
          throw new Exception("error: method " + funct.name + " cannot be applied to given types\n\t reason: actual and formal argument lists differ in length");
       }
       Iterator<Map.Entry<String,String>> funArgs = funct.arguments.entrySet().iterator();
       Iterator<Map.Entry<String,String>> callArgs = call.arguments.entrySet().iterator();
       while(funArgs.hasNext()){
          Map.Entry<String,String> funArgu = funArgs.next();
          Map.Entry<String,String> callArgu = callArgs.next();
          String funVal = funArgu.getValue();
          String callVal = callArgu.getValue();
          if(!isDerived(callVal, funVal) && !funVal.equals(callVal)){
            throw new Exception("error: incompatible types: " + callVal + " cannot be converted to " + funVal + "\n\tin " + funct.name + "(...)");
          }
        }
      }
      else if(funct.arguments.size() != 0){
          throw new Exception("error: method " + funct.name + " cannot be applied to given types\n\t reason: actual and formal argument lists differ in length");
      }
      field.name = name;
      field.type = funct.type;
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */

    public ReturnValue visit(ExpressionList n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FunctionInfo finfo = new FunctionInfo();
      ReturnValue val = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) val.typeValue;
      finfo.arguments.put("1", field.type);
      val = n.f1.accept(this, argu);
      FunctionInfo funct = (FunctionInfo) val.typeValue;
      Iterator<Map.Entry<String,String>> arguments = funct.arguments.entrySet().iterator();
      while(arguments.hasNext()){
        Map.Entry<String,String> arg = arguments.next();
        finfo.arguments.put(arg.getKey(), arg.getValue());
      }
      _ret.typeValue = finfo;
      return _ret;
   }
   
   /**
    * f0 -> ( ExpressionTerm() )*
    */
   public ReturnValue visit(ExpressionTail n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FunctionInfo finfo = new FunctionInfo();
      FieldInfo field = null;
      int counter = 2;
      if(n.f0.present()){
        for(int i = 0 ; i < n.f0.nodes.size() ; i++, counter++){
          ReturnValue val = n.f0.nodes.elementAt(i).accept(this, argu);
          Integer key = new Integer(counter);
          field = (FieldInfo) val.typeValue;
          finfo.arguments.put(key.toString(), field.type);
        }
      }
      _ret.typeValue = finfo;
      return _ret;
   }

   /**
    * f0 -> ","
    * f1 -> Expression()
    */
   public ReturnValue visit(ExpressionTerm n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      _ret = n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> NotExpression()
    *       | PrimaryExpression()
    */
   public ReturnValue visit(Clause n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      return _ret;
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
   public ReturnValue visit(PrimaryExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) _ret.typeValue;
      if(field.type.equals("Integer_literal")){
        field.type = "int";
        _ret.typeValue = field;
      }
      if(field.type.equals("Identifier") || field.type.equals("this")){
        _ret = idExists(field.name, argu);
      }
      return _ret;
   }

   /**
    * f0 -> <INTEGER_LITERAL>
    */
   public ReturnValue visit(IntegerLiteral n, FunctionInfo argu)throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      field.name = n.f0.toString();
      field.type = "Integer_literal";
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> "true"
    */
   public ReturnValue visit(TrueLiteral n, FunctionInfo argu)throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      field.type = "boolean";
      field.name = n.f0.toString();
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> "false"
    */
   public ReturnValue visit(FalseLiteral n, FunctionInfo argu)throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      field.type = "boolean";
      field.name = n.f0.toString();
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public ReturnValue visit(Identifier n, FunctionInfo argu)throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      field.type = "Identifier";
      field.name = n.f0.toString();
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> "this"
    */
   public ReturnValue visit(ThisExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      field.type = n.f0.toString();
      field.name = n.f0.toString();
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
   public ReturnValue visit(ArrayAllocationExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = null;
      ReturnValue val = n.f3.accept(this, argu);
      field = (FieldInfo) val.typeValue;
      field.name = "new int[" + field.name + "]";
      if(!field.type.equals("int")){
        throw new Exception("error: incompatible types: " + field.type + " cannot be converted to int\n\t(new int[" + field.name + "])");
      }
      field.type = "int[]";
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
   public ReturnValue visit(AllocationExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field;
      ReturnValue val = n.f1.accept(this, argu);
      field = (FieldInfo) val.typeValue;
       if(!ClassTable.containsKey(field.name)){
        throw new Exception("error: cannot find symbol " + field.name);
      }
      field.type = field.name;
      field.name = "new " + field.name + "()";
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> "!"
    * f1 -> Clause()
    */
   public ReturnValue visit(NotExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      FieldInfo field;
      _ret = n.f1.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      if(!field.type.equals("boolean")){
        throw new Exception("error: bad operand type " + field.type + " for unary operator '!',  (" + field.name + ")");
      }
      return _ret;
   }

   /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
   public ReturnValue visit(BracketExpression n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      FieldInfo field = null;
      _ret = n.f1.accept(this, argu);
      field = (FieldInfo) _ret.typeValue;
      field.name = "(" + field.name + ")";
      _ret.typeValue = field;
      return _ret;
   }

}
