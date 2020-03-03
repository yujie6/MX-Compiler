package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;
import Tools.Operators;

public class CmpInst extends Instruction {
    private CmpOperation SubOpcode;

    enum CmpOperation {
        eq, ne, ugt, uge, ult, ule, sgt, sge, slt, sle,
    }

    public CmpInst(BasicBlock parent, IRBaseType type, Operators.BinaryOp bop, Value LHS, Value RHS) {
        super(parent, InstType.icmp);
        this.UseList.add(new Use(LHS, this));
        this.UseList.add(new Use(RHS, this));
        if (bop.equals(Operators.BinaryOp.EQUAL)) {
            this.SubOpcode = CmpOperation.eq;
        } else if (bop.equals(Operators.BinaryOp.GREATER)) {
            this.SubOpcode = CmpOperation.sgt;
        } else if (bop.equals(Operators.BinaryOp.GREATER_EQUAL)) {
            this.SubOpcode = CmpOperation.sge;
        } else if (bop.equals(Operators.BinaryOp.LESS)) {
            this.SubOpcode = CmpOperation.slt;
        } else if (bop.equals(Operators.BinaryOp.LESS_EQUAL)) {
            this.SubOpcode = CmpOperation.sle;
        } else if (bop.equals(Operators.BinaryOp.NEQUAL)) {
            this.SubOpcode = CmpOperation.ne;
        }
        this.type = type;
    }

    public Value getLHS() {
        return this.UseList.get(0).getVal();
    }

    public Value getRHS() {
        return this.UseList.get(1).getVal();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(this.RegisterID);
        ans.append(" = icmp ").append(SubOpcode.toString());
        ans.append(" ").append(getLHS().getType().toString()).append(" ");
        ans.append(getRightValueLabel(getLHS())).append(", ");
        ans.append(getRightValueLabel(getRHS())).append("\n");
        return ans.toString();
    }

}
