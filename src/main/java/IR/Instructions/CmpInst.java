package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;
import Tools.Operators;

public class CmpInst extends Instruction {
    private CmpOperation SubOpcode;

    @Override
    public String toString() {
        return null;
    }

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


}
