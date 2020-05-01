package Target.RVInstructions;

import IR.BasicBlock;
import Target.RVBlock;
import Target.VirtualReg;

import java.util.ArrayList;

public class RVJump extends RVInstruction {

    RVBlock targetBB;

    public RVJump(RVOpcode opcode, RVBlock rvBlock, RVBlock target) {
        super(opcode, rvBlock);
        this.targetBB = target;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>();
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
