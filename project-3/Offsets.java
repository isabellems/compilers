import java.util.*;


class Offsets {
  public Map<String, Integer> members;
  public Map<String, Integer> functs;
  public int size;
  public int functLen;

  public Offsets(){
  	members = new LinkedHashMap<>();
    functs = new LinkedHashMap<>();
    size = 0;
    functLen = 0;
  }

  public Offsets(Offsets _offsets){
    Iterator<Map.Entry<String, Integer>> mem = _offsets.members.entrySet().iterator();
    Iterator<Map.Entry<String, Integer>> fun = _offsets.functs.entrySet().iterator();

    while(mem.hasNext()) {
        Map.Entry<String, Integer> member = mem.next();
        members.put(member.getKey(), member.getValue());
    }
     
    while(fun.hasNext()) {
       Map.Entry<String, Integer> member = fun.next();
       functs.put(member.getKey(), member.getValue());
    }
 }

  public void print() {
  	Iterator<Map.Entry<String, Integer>> mem = members.entrySet().iterator();
    while (mem.hasNext()) {
        Map.Entry<String, Integer> member = mem.next();
        System.out.println(member.getKey() + "   :   " + member.getValue());
    }

    Iterator<Map.Entry<String, Integer>> fun = functs.entrySet().iterator();
    while (fun.hasNext()) {
        Map.Entry<String, Integer> member = fun.next();
        System.out.println(member.getKey() + "   :   " + member.getValue());
    }
  }
}