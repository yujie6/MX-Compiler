package IR.Instructions;

import IR.BasicBlock;
import IR.Use;
import IR.Value;

public class StoreInst extends Instruction {

    public StoreInst(BasicBlock parent, Value storeValue, Value storeDest) {
        super(parent, InstType.store);
        this.UseList.add(new Use(storeValue, this));
        this.UseList.add(new Use(storeDest, this));
        this.type = null; // should be ?
    }

    public Value getStoreDest() {
        return this.UseList.get(0).getVal();
    }

    public Value getStoreValue() {
        return this.UseList.get(1).getVal();
    }
}
