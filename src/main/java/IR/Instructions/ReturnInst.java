package IR.Instructions;

import IR.*;
import IR.Types.IRBaseType;

public class ReturnInst extends Instruction {
    private IRBaseType RetType;

    public ReturnInst(BasicBlock parent, IRBaseType retType, Value retValue) {
        super(parent, InstType.ret);
        this.Opcode = InstType.ret;
        this.RetType = retType;
        if (retValue != null)
            this.UseList.add(new Use(retValue, this));
    }

    public IRBaseType getRetType() {
        return RetType;
    }

    public Value getRetValue() {
        return UseList.get(0).getVal();
    }

    @Override
    public void copyTo(BasicBlock other, IRMap irMap) {
        other.AddInstAtTail(new ReturnInst(other, RetType, irMap.get(getRetValue())));
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("ret ");
        ans.append(getRetType().toString()).append(" ");
        if (!getRetType().toString().equals("void")) {
            ans.append(getRightValueLabel(getRetValue()));
        }
        return ans.toString();
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
