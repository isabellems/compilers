import java.util.*;

class ClassInfo extends TypeValue {
	public String parent;
	public Map<String, String> members;
	public Map<String, FunctionInfo> functions;
	public int memOffset;
	public int funOffset;


	public ClassInfo(){
		parent = null;
		members = new LinkedHashMap<String, String>();
		functions = new LinkedHashMap<String, FunctionInfo>();
		memOffset = 0;
		funOffset = 0;
	}
}

