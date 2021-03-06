package Target.RVInstructions;

import Target.Immediate;
import Target.RVBlock;
import Target.VirtualReg;

import java.util.Set;

public class LI extends RVInstruction {
    Immediate immediate;
    VirtualReg destReg;
    public LI(RVBlock rvBlock, Immediate imm, VirtualReg destReg) {
        super(RVOpcode.li, rvBlock);
        this.immediate = imm;
        this.destReg=  destReg;
    }

    @Override
    public Set<VirtualReg> getUseRegs() {
        return Set.of();
    }

    @Override
    public Set<VirtualReg> getDefRegs() {
        return Set.of(this.destReg);
    }

    @Override
    public void replaceDef(VirtualReg t) {
        if (this.destReg.equals(t)) {
            this.destReg = t;
        }
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        // should never access
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString()).append(",\t");
        ans.append(immediate.toString()).append("\n");
        return ans.toString();
    }
}
