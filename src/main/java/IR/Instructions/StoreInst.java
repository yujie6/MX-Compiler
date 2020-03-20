package IR.Instructions;

import BackEnd.IRBuilder;
import IR.BasicBlock;
import IR.Constants.Constant;
import IR.Types.IRBaseType;
import IR.Types.PointerType;
import IR.Use;
import IR.Value;
import Tools.MXError;

public class StoreInst extends Instruction {

    boolean StoreConst;

    public StoreInst(BasicBlock parent, Value storeValue, Value storeDest) {
        super(parent, InstType.store);
        this.StoreConst = (storeValue instanceof Constant);
        if (storeValue == null) {
            throw new MXError("fuck ");
        }
        if (!(storeDest.getType() instanceof PointerType)) {
            IRBuilder.logger.severe("Fatal error: store operand must be a pointer!");
            System.exit(1);
        }
        this.UseList.add(new Use(storeValue, this));
        this.UseList.add(new Use(storeDest, this));
        this.type = null; // should be ?
    }

    public Value getStoreDest() {
        return this.UseList.get(1).getVal();
    }

    public Value getStoreValue() {
        return this.UseList.get(0).getVal();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("store ");
        ans.append(getStoreValue().getType().toString()).append(" ");
        ans.append(getRightValueLabel(getStoreValue())).append(", ");
        ans.append(getStoreDest().getType().toString()).append(" ");
        ans.append( getRightValueLabel(getStoreDest()) );
        ans.append(", align 4\n");
        return ans.toString();
    }
}
