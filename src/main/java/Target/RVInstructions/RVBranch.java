package Target.RVInstructions;

import Target.Immediate;
import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

import java.util.*;

public class RVBranch extends RVInstruction {

    VirtualReg LHS, RHS;
    Immediate offset;


    public RVBranch(RVOpcode opcode, RVBlock rvBlock, RVOperand lhs, RVOperand rhs) {
        super(opcode, rvBlock);
        this.RHS = (VirtualReg) rhs;
        this.LHS = (VirtualReg) lhs;
    }

    public void setOffset(Immediate offset) {
        this.offset = offset;
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
    public String toString() {
        return null;
    }
}
