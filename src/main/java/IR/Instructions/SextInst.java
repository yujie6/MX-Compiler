package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;

public class SextInst extends Instruction {
    // sign extend on integer
    private IRBaseType BaseType, TargetType;

    public SextInst(BasicBlock parent, IRBaseType baseType, Value extendValue, IRBaseType targetType) {
        super(parent, InstType.sext);
        this.UseList.add(new Use(extendValue, this));
        this.BaseType = baseType;
        this.TargetType = targetType;
    }

    public Value getExtendValue() {
        return this.UseList.get(0).getVal();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(RegisterID);
        ans.append(" = sext ").append(this.BaseType.toString());
        ans.append(" ").append(getRightValueLabel(getExtendValue()));
        ans.append(" to ").append(this.TargetType.toString()).append("\n");
        return ans.toString();
    }

    public IRBaseType getBaseType() {
        return BaseType;
    }

    public IRBaseType getTargetType() {
        return TargetType;
    }
}
