package Target.RVInstructions;

import Target.RVBlock;
import Target.VirtualReg;

import java.util.ArrayList;
import java.util.Set;

public class RVJump extends RVInstruction {

    RVBlock targetBB;

    public RVJump(RVOpcode opcode, RVBlock rvBlock, RVBlock target) {
        super(opcode, rvBlock);
        this.targetBB = target;
    }

    @Override
    public Set<VirtualReg> getUseRegs() {
        return Set.of();
    }

    @Override
    public Set<VirtualReg> getDefRegs() {
        return Set.of();
    }

    @Override
    public void replaceDef(VirtualReg t) {
        // never accessed
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        // never
    }

    @Override
    public String toString() {
        return "j\t" + targetBB.toString() + "\n";
    }
}
