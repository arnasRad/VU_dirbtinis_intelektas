import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {

        Map<String, Integer> map = new HashMap<>();

        System.out.println(map.containsKey("a"));
        System.out.println(map.get("a"));
        System.out.println();

        map.put("a", 0);
        System.out.println(map.containsKey("a"));
        System.out.println(map.get("a"));
        System.out.println();

        map.put("a", 1);
        System.out.println(map.containsKey("a"));
        System.out.println(map.get("a"));
        System.out.println();
    }
}
