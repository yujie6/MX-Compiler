package Target.RVInstructions;

import Target.RVBlock;
import Target.RVFunction;
import Target.VirtualReg;

import java.util.ArrayList;

public class RVCall extends RVInstruction{
    private RVFunction callee;
    public RVCall(RVBlock rvBlock, RVFunction callee) {
        super(RVOpcode.call, rvBlock);
        this.callee = callee;
    }

    public RVFunction getCallee() {
        return callee;
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
        return "call " + callee.getIdentifier() + "\n";
    }
}
