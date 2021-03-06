package IR.Instructions;

import BackEnd.IRBuilder;
import IR.*;
import IR.Constants.Constant;
import IR.Types.ArrayType;
import IR.Types.IRBaseType;
import IR.Types.PointerType;
import Tools.MXError;

public class StoreInst extends Instruction {

    boolean StoreConst;

    public StoreInst(BasicBlock parent, Value storeValue, Value storeDest) {
        super(parent, InstType.store);
        this.StoreConst = (storeValue instanceof Constant);
        if (storeValue == null) {
            throw new MXError("fuck ");
        }
        if (!(storeDest.getType() instanceof PointerType || storeDest.getType() instanceof ArrayType
        || storeDest instanceof GetPtrInst)) {
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
    public void copyTo(BasicBlock other, IRMap irMap) {
        other.AddInstAtTail(new StoreInst(other, irMap.get(getStoreValue()), irMap.get(getStoreDest())));
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("store ");
        ans.append(getStoreValue().getType().toString()).append(" ");
        ans.append(getRightValueLabel(getStoreValue())).append(", ");
        ans.append(getStoreDest().getType().toString()).append(" ");
        ans.append( getRightValueLabel(getStoreDest()) );
        ans.append(", align 8\n");
        return ans.toString();
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
