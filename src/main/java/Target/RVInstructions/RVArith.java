package Target.RVInstructions;

import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

public class RVArith extends RVInstruction {

    RVOperand LHS, RHS;
    VirtualReg destReg;


    public RVArith(RVOpcode opcode, RVBlock rvBlock, RVOperand lhs, RVOperand rhs, VirtualReg dest) {
        super(opcode, rvBlock);
        this.LHS = lhs;
        this.RHS = rhs;
        this.destReg = dest;
    }

    @Override
    public String toString() {
        return null;
    }
}
