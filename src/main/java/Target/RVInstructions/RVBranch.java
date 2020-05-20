package Target.RVInstructions;

import Target.Immediate;
import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

import java.util.*;

public class RVBranch extends RVInstruction {

    VirtualReg LHS, RHS;
    RVBlock target;


    public RVBranch(RVOpcode opcode, RVBlock rvBlock, RVOperand lhs, RVOperand rhs, RVBlock target) {
        super(opcode, rvBlock);
        this.RHS = (VirtualReg) rhs;
        this.LHS = (VirtualReg) lhs;
        this.target = target;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>(List.of(LHS, RHS));
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>();
    }

    @Override
    public void replaceDef(VirtualReg t) {
        // this should never be accessed!
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        if (old.equals(LHS)) {LHS = replaceVal; }
        if (old.equals(RHS)) {RHS = replaceVal; }
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(LHS.toString()).append(",\t").append(RHS.toString());
        ans.append(",\t").append(target.toString()).append("\n");
        return ans.toString();
    }
}
