package Target;

import BackEnd.InstSelector;
import IR.Argument;
import IR.GlobalVariable;
import IR.Instructions.AllocaInst;
import IR.Instructions.Instruction;

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
        this.identifier = Instruction.getRightValueLabel(allocaInst);
        this.offset = rvFunction.allocaOnStack();
        this.baseAddrReg = InstSelector.fakePhyRegMap.get("sp");
    }

    public RVAddr(Argument arg, RVFunction rvFunction) {
        // on stack
        this.onStack = true;
        this.isGlobal = false;
        this.identifier = Instruction.getRightValueLabel(arg);
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

    public void resetStackAddr(int deltaStack) {
        this.baseAddrReg = InstSelector.fakePhyRegMap.get("s0");
        // this.offset = deltaStack + this.offset;
    }

    public VirtualReg getBaseAddrReg() {
        return baseAddrReg;
    }

    public String toString() {
        if (onStack)
            return this.offset + "(" + this.baseAddrReg.toString()+")";
        else if (isGlobal)
            return "%lo(" + this.identifier + ")(" + this.baseAddrReg.toString() + ")";
        else
            return this.offset + "(" + this.baseAddrReg.toString() + ")";
    }
}
