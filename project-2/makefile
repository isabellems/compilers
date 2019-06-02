all: compile

compile:
	java -jar jtb132di.jar -te minijava.jj
	java -jar javacc5.jar minijava-jtb.jj
	javac ClassInfo.java
	javac FunctionInfo.java
	javac FieldInfo.java
	javac ReturnValue.java
	javac TypeValue.java
	javac SymbolTable.java
	javac MyTypeChecker.java
	javac VTable.java
	javac Main.java

clean:
	rm -f *.class *~