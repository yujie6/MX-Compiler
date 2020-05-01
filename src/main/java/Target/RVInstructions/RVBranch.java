package Target.RVInstructions;

import Target.Immediate;
import Target.RVBlock;
import Target.RVInstructions.RVOpcode;
import Target.RVOperand;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RVBranch extends RVInstruction {

    RVOperand LHS, RHS;
    Immediate offset;


    public RVBranch(RVOpcode opcode, RVBlock rvBlock, RVOperand lhs, RVOperand rhs) {
        super(opcode, rvBlock);
        this.RHS = rhs;
        this.LHS = lhs;
    }

    public void setOffset(Immediate offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return null;
    }
}
