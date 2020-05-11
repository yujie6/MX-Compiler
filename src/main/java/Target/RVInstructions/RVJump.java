package Target.RVInstructions;

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
    public ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "j\t" + targetBB.toString() + "\n";
    }
}
