package IR.Instructions;

import IR.BasicBlock;
import IR.Value;

public class StoreInst extends Instruction {
    private Value StoreValue, StoreDest;
    public StoreInst(BasicBlock parent, Value storeValue, Value storeDest) {
        super(parent);
        this.StoreDest = storeDest;
        this.StoreValue = storeValue;
    }

    public Value getStoreDest() {
        return StoreDest;
    }

    public Value getStoreValue() {
        return StoreValue;
    }
}
