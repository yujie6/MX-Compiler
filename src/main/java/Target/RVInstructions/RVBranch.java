package Target.RVInstructions;

import Target.Immediate;
import Target.RVBlock;
import Target.RVInstructions.RVOpcode;
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
    public VirtualReg getDefReg() {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }
}
