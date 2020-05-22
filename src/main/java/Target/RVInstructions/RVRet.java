package Target.RVInstructions;

import Target.RVBlock;
import Target.VirtualReg;

import java.util.Set;

public class RVRet extends RVInstruction {

    public RVRet(RVBlock rvBlock) {
        super(RVOpcode.ret, rvBlock);
    }

    @Override
    public Set<VirtualReg> getUseRegs() {
        return Set.of();
    }

    @Override
    public
    Set<VirtualReg> getDefRegs() {
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
        return "ret\n";
    }
}
