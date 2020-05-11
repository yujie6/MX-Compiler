package Target;

import BackEnd.InstSelector;
import IR.GlobalVariable;
import IR.Instructions.AllocaInst;

public class RVAddr extends RVOperand {
    String identifier;
    AllocaInst allocaInst;
    VirtualReg baseAddrReg;
    int offset;
    private boolean onStack;
    private boolean isGlobal;

    public RVAddr(AllocaInst allocaInst, RVFunction rvFunction) {
        // on stack
        this.onStack = true;
        this.isGlobal = false;
        this.allocaInst = allocaInst;
        this.identifier = allocaInst.getRightValueLabel(allocaInst);
        this.offset = rvFunction.allocaOnStack();
        this.baseAddrReg = InstSelector.fakePhyRegMap.get("sp");
    }

    public RVAddr(VirtualReg baseReg, int offset) {
        this.onStack = false;
        this.isGlobal = false;
        this.baseAddrReg = baseReg;
        this.offset = offset;
    }

    public RVAddr(RVGlobal gvar, VirtualReg base) {
        this.identifier = gvar.getIdentifier();
        this.onStack = false;
        this.baseAddrReg = base;
        this.isGlobal = true;
    }

    public VirtualReg getBaseAddrReg() {
        return baseAddrReg;
    }

    public String toString() {
        if (onStack)
            return this.offset + "(sp)";
        else if (isGlobal)
            return "%lo(" + this.identifier + ")(" + this.baseAddrReg.toString() + ")";
        else
            return this.offset + "(" + this.baseAddrReg.toString() + ")";
    }
}
