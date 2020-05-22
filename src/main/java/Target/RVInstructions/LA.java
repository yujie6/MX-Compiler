package Target.RVInstructions;

import Target.RVBlock;
import Target.RVGlobal;
import Target.VirtualReg;

import java.util.Set;

public class LA extends RVInstruction {
    VirtualReg destReg;
    RVGlobal symbolAddr;
    public LA(RVBlock rvBlock, RVGlobal addr, VirtualReg destReg) {
        super(RVOpcode.la, rvBlock);
        this.symbolAddr = addr;
        this.destReg = destReg;
    }

    @Override
    public Set<VirtualReg> getUseRegs() {
        return Set.of();
    }

    @Override
    public Set<VirtualReg> getDefRegs() {
        return Set.of(destReg);
    }

    @Override
    public void replaceDef(VirtualReg t) {
        if (destReg.equals(t)) {
            this.destReg = t;
        }
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {

    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString()).append(",\t");
        ans.append(symbolAddr.toString()).append("\n");
        return ans.toString();
    }
}
