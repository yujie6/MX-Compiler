package IR.Instructions;

import IR.*;
import IR.Module;
import IR.Types.IRBaseType;
import Tools.Operators;

public class CmpInst extends Instruction {
    public CmpOperation SubOpcode;

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }

    public enum CmpOperation {
        eq, ne, ugt, uge, ult, ule, sgt, sge, slt, sle,
    }

    public CmpInst(BasicBlock parent, Operators.BinaryOp bop, Value LHS, Value RHS) {
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
        this.type = Module.I1;
    }

    public CmpInst(BasicBlock parent, CmpOperation subOpcode, Value LHS, Value RHS) {
        super(parent, InstType.icmp);
        this.UseList.add(new Use(LHS, this));
        this.UseList.add(new Use(RHS, this));
        this.SubOpcode = subOpcode;
        this.type = Module.I1;
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

    public String getFullOpcode () {
        return "icmp " + SubOpcode.toString();
    }

    @Override
    public void copyTo(BasicBlock other, IRMap irMap) {
        other.AddInstAtTail(new CmpInst(other, SubOpcode, irMap.get(getLHS()), irMap.get(getRHS())));
    }

    @Override
    public boolean isCommutative() {
        return SubOpcode.equals(CmpOperation.eq) || SubOpcode.equals(CmpOperation.ne);
    }
}
