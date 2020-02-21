package IR;

import java.util.HashMap;

public class ValueSymbolTable {
    private HashMap<String, Value> valueHashMap;

    public ValueSymbolTable() {
        valueHashMap = new HashMap<>();
    }

    public void put(String id, Value var) {
        valueHashMap.put(id, var);
    }

    public Value get(String id) {
        return valueHashMap.get(id);
    }
}
