package Target.RVInstructions;

import Target.RVBlock;
import Target.VirtualReg;

import java.util.ArrayList;

public class RVCall extends RVInstruction{
    public RVCall(RVOpcode opcode, RVBlock rvBlock) {
        super(opcode, rvBlock);
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return null;
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }
}
