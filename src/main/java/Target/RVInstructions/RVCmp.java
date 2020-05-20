package Target.RVInstructions;

import Target.RVBlock;
import Target.VirtualReg;

import java.util.ArrayList;
import java.util.List;

public class RVCmp extends RVInstruction {
    VirtualReg srcReg, destReg;

    public RVCmp(RVOpcode opcode, RVBlock rvBlock, VirtualReg src, VirtualReg dest) {
        super(opcode, rvBlock);
        this.srcReg = src;
        this.destReg = dest;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>(List.of(srcReg));
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>(List.of(destReg));
    }

    @Override
    public void replaceDef(VirtualReg t) {
        if (t.equals(destReg)) {
            this.destReg = t;
        }
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        if (srcReg.equals(old)) {
            this.srcReg = replaceVal;
        }
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString());
        ans.append("\t").append(srcReg.toString());
        ans.append("\n");
        return ans.toString();
    }
}
