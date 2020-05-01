package IR.Instructions;

import IR.BasicBlock;
import IR.IRVisitor;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;

public class SextInst extends Instruction {
    // sign extend on integer
    private IRBaseType BaseType;

    public SextInst(BasicBlock parent, IRBaseType baseType, Value extendValue, IRBaseType targetType) {
        super(parent, InstType.sext);
        this.UseList.add(new Use(extendValue, this));
        this.BaseType = baseType;
        this.type = targetType;
    }

    public Value getExtendValue() {
        return this.UseList.get(0).getVal();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(RegisterID);
        ans.append(" = sext ").append(this.BaseType.toString());
        ans.append(" ").append(getRightValueLabel(getExtendValue()));
        ans.append(" to ").append(this.type.toString()).append("\n");
        return ans.toString();
    }

    public IRBaseType getBaseType() {
        return BaseType;
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
