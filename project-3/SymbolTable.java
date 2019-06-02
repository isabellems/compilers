import java.util.*;
import syntaxtree.*;
import visitor.GJDepthFirst;

/**
 * Provides default methods which visit each node in the tree in depth-first
 * order.  Your visitors may extend this class.
 */
public class SymbolTable extends GJDepthFirst<ReturnValue, FunctionInfo> {

  public static Map<String, ClassInfo> ClassTable;
  public static FunctionInfo MainTable;

  public SymbolTable(){
      ClassTable = new LinkedHashMap<>();
      MainTable = new FunctionInfo();
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
      ReturnValue _ret = null;
      ClassInfo clInfo = new ClassInfo();
      ReturnValue val = n.f1.accept(this, argu);
      MainTable.owner = val.value;
      clInfo.name = val.value;
      MainTable.name = n.f6.toString();
      val = n.f11.accept(this, argu);
      MainTable.arguments.put(val.value, "String[]");
      if(n.f14.present()){
         for(int i = 0 ; i < n.f14.nodes.size(); i++ ) {
          val = n.f14.nodes.elementAt(i).accept(this, argu);
          FieldInfo field = (FieldInfo) val.typeValue;
          MainTable.identifiers.put(field.name, field.type);
        }
      }
      clInfo.functions.put("main", MainTable);
      ClassTable.put(MainTable.owner, clInfo);
      n.f15.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
   public ReturnValue visit(TypeDeclaration n, FunctionInfo argu) throws Exception {
      ReturnValue _ret=null;
      n.f0.accept(this, argu);
      return _ret;
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
      ClassInfo info = new ClassInfo();
      ReturnValue val = n.f1.accept(this, argu);
      info.name = val.value;
      if(n.f3.present()){
        for(int i = 0 ; i < n.f3.nodes.size(); i++ ) {
            ReturnValue value = n.f3.nodes.elementAt(i).accept(this, argu);
            FieldInfo field = (FieldInfo) value.typeValue;
            info.members.put(field.name, field.type);
        }
      }
      if(n.f4.present()){
        for(int i = 0 ; i < n.f4.nodes.size(); i++ ) {
            ReturnValue value = n.f4.nodes.elementAt(i).accept(this, argu);
            FunctionInfo funct = (FunctionInfo) value.typeValue;
            funct.owner = info.name;
            info.functions.put(funct.name, funct);
        }
      }

      n.f4.accept(this, argu);
      ClassTable.put(info.name, info);
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
      ReturnValue _ret=null;
      ClassInfo info = new ClassInfo();
      ReturnValue val = n.f1.accept(this, argu);
      info.name = val.value;
      val = n.f3.accept(this, argu);
      info.parent = val.value;
      if(n.f5.present()){
        for(int i = 0 ; i < n.f5.nodes.size(); i++ ) {
            ReturnValue value = n.f5.nodes.elementAt(i).accept(this, argu);
            FieldInfo field = (FieldInfo) value.typeValue;
            info.members.put(field.name, field.type);
        }
      }
      if(n.f6.present()){
         for(int i = 0 ; i < n.f6.nodes.size(); i++ ) {
            ReturnValue value = n.f6.nodes.elementAt(i).accept(this, argu);
            FunctionInfo funct = (FunctionInfo) value.typeValue;
            funct.owner = info.name;
            String parent = info.parent;
            while(parent != null){
              ClassInfo parentInfo = ClassTable.get(parent);
              if(parentInfo.functions.containsKey(funct.name)){
                FunctionInfo parFunct = parentInfo.functions.get(funct.name);
                if(!parFunct.type.equals(funct.type)){
                  throw new Exception("error: " + funct.name + " in " + info.name + " cannot override " + funct.name + " in " + parentInfo.name + "\n return type " + funct.type + " is not compatible with " + parFunct.type);
                }
                Boolean difArgs = false;
                Boolean firstLoop = true;
                String argsStr = "";
                Iterator<Map.Entry<String, String>> protoArgs = parFunct.arguments.entrySet().iterator();
                Iterator<Map.Entry<String, String>> functArgs = funct.arguments.entrySet().iterator();
                while (protoArgs.hasNext()) {
                    Map.Entry<String, String> protoArgu = protoArgs.next();
                    Map.Entry<String, String> functArgu = functArgs.next();
                    if (firstLoop){
                      argsStr = argsStr + protoArgu.getValue();
                      firstLoop = false;
                    }
                    else{
                      argsStr = argsStr + " , " + protoArgu.getValue();
                    }
                    if(!protoArgu.getValue().equals(functArgu.getValue())){
                      difArgs = true;
                    }
                }
                if(parFunct.arguments.size() != funct.arguments.size() || difArgs){
                  throw new Exception("error: " + funct.name + " in " + info.name + " cannot override " + funct.name + "(" + argsStr + ")" + " in " + parentInfo.name);
                }
              }
              parent = parentInfo.parent;
            }
            info.functions.put(funct.name, funct);
        }
      }
      ClassTable.put(info.name, info);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
   public ReturnValue visit(VarDeclaration n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      ReturnValue val;
      val = n.f0.accept(this, argu);
      field.type = val.value;
      val = n.f1.accept(this, argu);
      field.name = val.value;
      _ret.typeValue = field;
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
      FunctionInfo funct = new FunctionInfo();
      ReturnValue val = n.f1.accept(this, argu);
      funct.type = val.value;
      val = n.f2.accept(this, argu);
      funct.name = val.value;
      if(n.f4.present()){
        n.f4.accept(this, funct);
      }
      String argStr = "";
      Boolean firstLoop = true;
      Iterator<Map.Entry<String, String>> arguments = funct.arguments.entrySet().iterator();

      while (arguments.hasNext()) {
        Map.Entry<String, String> argEntry = arguments.next();
        if(firstLoop){
          argStr = argStr + argEntry.getValue();
          firstLoop = false;
        }
        else{
          argStr = argStr + " , " + argEntry.getValue();
        }
      }

      if(n.f7.present()){
        for(int i = 0 ; i < n.f7.nodes.size(); i++ ) {
            val = n.f7.nodes.elementAt(i).accept(this, argu);
            FieldInfo field = (FieldInfo) val.typeValue;
            funct.identifiers.put(field.name, field.type);
        }
      }
      _ret.typeValue = funct;
      return _ret;
   }

   /**
    * f0 -> FormalParameter()
    * f1 -> FormalParameterTail()
    */
   public ReturnValue visit(FormalParameterList n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = null;
      ReturnValue val = n.f0.accept(this, argu);
      FieldInfo field = (FieldInfo) val.typeValue;
      argu.arguments.put(field.name, field.type);
      n.f1.accept(this, argu);
      return _ret;
   }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    */
   public ReturnValue visit(FormalParameter n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      FieldInfo field = new FieldInfo();
      ReturnValue val = n.f0.accept(this, argu);
      field.type = val.value;
      val = n.f1.accept(this, argu);
      field.name = val.value;
      _ret.typeValue = field;
      return _ret;
   }

   /**
    * f0 -> ( FormalParameterTerm() )*
    */
   public ReturnValue visit(FormalParameterTail n, FunctionInfo argu) throws Exception {
      ReturnValue _ret=null;
      if(n.f0.present()){
         for(int i = 0 ; i < n.f0.nodes.size(); i++ ) {
            ReturnValue val = n.f0.nodes.elementAt(i).accept(this, argu);
            FieldInfo field = (FieldInfo) val.typeValue;
            argu.arguments.put(field.name, field.type);
         }
      }
      n.f0.accept(this, argu);
      return _ret;
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
      ReturnValue _ret = new ReturnValue();
      _ret.value = n.f0.accept(this, argu).value;
      return _ret;
   }

   /**
    * f0 -> "int"
    * f1 -> "["
    * f2 -> "]"
    */
   public ReturnValue visit(ArrayType n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      _ret.value = n.f0.toString() + n.f1.toString() + n.f2.toString();
      return _ret;
   }

   /**
    * f0 -> "boolean"
    */
   public ReturnValue visit(BooleanType n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      _ret.value = n.f0.toString();
      return _ret;
   }

   /**
    * f0 -> "int"
    */
   public ReturnValue visit(IntegerType n, FunctionInfo argu) throws Exception {
      ReturnValue _ret = new ReturnValue();
      _ret.value = n.f0.toString();
      return _ret;
   }

   /**
    * f0 -> <IDENTIFIER>
    */
   public ReturnValue visit(Identifier n, FunctionInfo argu)throws Exception {
      ReturnValue _ret = new ReturnValue();
      _ret.value = n.f0.toString();
      return _ret;
   }

}
