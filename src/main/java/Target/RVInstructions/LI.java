package Target.RVInstructions;

import Target.Immediate;
import Target.RVBlock;
import Target.VirtualReg;

import java.util.ArrayList;
import java.util.List;

public class LI extends RVInstruction {
    Immediate immediate;
    VirtualReg destReg;
    public LI(RVBlock rvBlock, Immediate imm, VirtualReg destReg) {
        super(RVOpcode.li, rvBlock);
        this.immediate = imm;
        this.destReg=  destReg;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>(List.of(this.destReg));
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
