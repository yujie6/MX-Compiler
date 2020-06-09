package IR.Instructions;


import IR.*;
import IR.Types.IRBaseType;
import Tools.Operators;

import java.io.IOException;

public class BinOpInst extends Instruction {

    public BinOpInst(BasicBlock parent, IRBaseType type, InstType opcode, Value LHS, Value RHS) {
        super(parent, opcode);
        this.type = type;
        this.UseList.add(new Use(LHS, this));
        this.UseList.add(new Use(RHS, this));
    }

    public Value getLHS() {
        return UseList.get(0).getVal();
    }

    public Value getRHS() {
        return UseList.get(1).getVal();
    }

    @Override
    public void copyTo(BasicBlock other, IRMap irMap) {
        other.AddInstAtTail(new BinOpInst(other, type, Opcode, irMap.get(getLHS()) , irMap.get(getRHS())));
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(RegisterID + " = " + this.getOpcode());
        ans.append(" ").append(type.toString()).append(" ");
        ans.append( getRightValueLabel(getLHS()) ).append(", ");
        ans.append( getRightValueLabel(getRHS()) ).append("\n");
        return ans.toString();
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
