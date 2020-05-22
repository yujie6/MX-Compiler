package Target.RVInstructions;

import BackEnd.InstSelector;
import Target.RVBlock;
import Target.RVFunction;
import Target.RVTargetInfo;
import Target.VirtualReg;

import java.util.HashSet;
import java.util.Set;

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
    public Set<VirtualReg> getUseRegs() {
        HashSet<VirtualReg> useRegs = new HashSet<>();
        for (int i = 0; i < callee.getArgNum() && i < 8; i++) {
            useRegs.add(InstSelector.fakePhyRegMap.get("a" + i));
        }
        return useRegs;
    }

    @Override
    public Set<VirtualReg> getDefRegs() {
        HashSet<VirtualReg> defRegs = new HashSet<>();
        for (String name : RVTargetInfo.callerSaves) {
            defRegs.add(InstSelector.fakePhyRegMap.get(name));
        }
        return defRegs;
    }

    @Override
    public void replaceDef(VirtualReg t) {
        // this should never be accessed
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        // never
    }

    @Override
    public String toString() {
        return "call " + callee.getIdentifier() + "\n";
    }
}
