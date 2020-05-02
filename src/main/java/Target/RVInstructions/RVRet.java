package Target.RVInstructions;

import Target.RVBlock;
import Target.VirtualReg;

import java.util.ArrayList;

public class RVRet extends RVInstruction {

    public RVRet(RVBlock rvBlock) {
        super(RVOpcode.ret, rvBlock);
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>();
    }

    @Override
    public
    ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>();
    }

    @Override
    public String toString() {
        return "ret\n";
    }
}
