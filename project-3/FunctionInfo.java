import java.util.*;

class FunctionInfo  extends TypeValue{
	public String type; 
	public String owner;
	public Map<String, String> arguments;
	public Map<String, String> identifiers;

	public FunctionInfo(){
		arguments = new LinkedHashMap<String, String>();
		identifiers = new LinkedHashMap<String, String>();
	}
}