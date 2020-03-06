package IR;

import java.util.HashMap;

public class ValueSymbolTable {
    private HashMap<String, Value> valueHashMap;

    public ValueSymbolTable() {
        valueHashMap = new HashMap<>();
    }

    public ValueSymbolTable(HashMap<String, Value> hashMap) {
        valueHashMap = hashMap;
    }

    public void put(String id, Value var) {
        valueHashMap.put(id, var);
    }

    public Value get(String id) {
        return valueHashMap.get(id);
    }

    public boolean contains(String key) {
        return valueHashMap.containsKey(key);
    }

    @Override
    public ValueSymbolTable clone() {
        HashMap<String, Value> hashMap = new HashMap<>(valueHashMap);
        return new ValueSymbolTable(hashMap);
    }
}
