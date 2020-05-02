package Target.RVInstructions;

import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

import java.util.ArrayList;
import java.util.List;

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
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>(List.of(LHS, RHS));
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>(List.of(destReg));
    }

    @Override
    public String toString() {
        return null;
    }
}
