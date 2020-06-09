package IR;
import java.util.HashMap;

public class IRMap {
    private HashMap<Value, Value> valueMap;
    private HashMap<BasicBlock, BasicBlock> blockMap;


    public IRMap() {
        this.valueMap = new HashMap<>();
        this.blockMap = new HashMap<>();
    }

    public void put(BasicBlock origin, BasicBlock replaceVal) {
        this.blockMap.put(origin, replaceVal);
    }

    public void put(Value origin, Value replaceVal) {
        this.valueMap.put(origin, replaceVal);
    }

    public Value get(Value origin) {
        return this.valueMap.get(origin);
    }

    public BasicBlock get(BasicBlock BB) {
        return this.blockMap.get(BB);
    }

    public void clear() {
        valueMap.clear();
        blockMap.clear();
    }
}
