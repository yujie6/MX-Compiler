package IR.Instructions;

import IR.*;
import IR.Types.IRBaseType;

public class BitCastInst extends Instruction {

    private IRBaseType TargetType;

    public BitCastInst(BasicBlock parent, Value castValue, IRBaseType targetType) {
        super(parent, InstType.bitcast);
        this.UseList.add(new Use(castValue, this));
        this.TargetType = targetType;
        this.type = targetType;
    }

    public Value getCastValue() {
        return UseList.get(0).getVal();
    }

    @Override
    public void copyTo(BasicBlock other, IRMap irMap) {
        other.AddInstAtTail(new BitCastInst(other, irMap.get(getCastValue()), TargetType));
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(RegisterID);
        ans.append(" = bitcast ").append(getCastValue().getType().toString());
        ans.append(" ").append(getRightValueLabel(getCastValue()));
        ans.append(" to ").append(TargetType.toString()).append("\n");
        return ans.toString();
    }

    public IRBaseType getTargetType() {
        return TargetType;
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
