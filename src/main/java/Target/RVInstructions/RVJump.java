package Target.RVInstructions;

import IR.BasicBlock;
import Target.RVBlock;

public class RVJump extends RVInstruction {

    RVBlock targetBB;

    public RVJump(RVOpcode opcode, RVBlock rvBlock, RVBlock target) {
        super(opcode, rvBlock);
        this.targetBB = target;
    }

    @Override
    public String toString() {
        return null;
    }
}
