package Target.RVInstructions;

import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

import java.util.Set;

public class RVArith extends RVInstruction {

    VirtualReg LHS, RHS;
    VirtualReg destReg;


    public RVArith(RVOpcode opcode, RVBlock rvBlock, RVOperand lhs, RVOperand rhs, VirtualReg dest) {
        super(opcode, rvBlock);
        this.LHS = (VirtualReg) lhs;
        this.RHS = (VirtualReg) rhs;
        this.destReg = dest;
    }

    @Override
    public Set<VirtualReg> getUseRegs() {
        if (LHS.equals(RHS)) return Set.of(LHS);
        return Set.of(LHS, RHS);
    }

    @Override
    public Set<VirtualReg> getDefRegs() {
        return Set.of(destReg);
    }

    @Override
    public void replaceDef(VirtualReg t) {
        this.destReg = t;
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        if (old.equals(LHS)) {LHS = replaceVal; }
        if (old.equals(RHS)) {RHS = replaceVal; }
        // never here
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString()).append(",\t").append(LHS.toString());
        ans.append(",\t").append(RHS.toString()).append("\n");
        return ans.toString();
    }
}
