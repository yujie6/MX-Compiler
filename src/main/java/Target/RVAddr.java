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
    RVFunction function;
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
        this.function = rvFunction;
        this.baseAddrReg = InstSelector.fakePhyRegMap.get("sp");
    }

    public RVAddr(Argument arg, RVFunction rvFunction) {
        // on stack
        this.onStack = true;
        this.isGlobal = false;
        this.function = rvFunction;
        this.identifier = Instruction.getRightValueLabel(arg);
        this.offset = rvFunction.allocaOnStack();
        this.baseAddrReg = InstSelector.fakePhyRegMap.get("sp");
    }

    public RVAddr(VirtualReg baseReg, int offset, RVFunction rvFunction) {
        this.onStack = false;
        this.isGlobal = false;
        this.baseAddrReg = baseReg;
        this.function = rvFunction;
        this.offset = offset;
        if (baseReg.toString().equals("sp")) {
            this.onStack = true;
        }
    }

    public RVAddr(RVGlobal gvar, VirtualReg base, RVFunction rvFunction) {
        this.identifier = gvar.getIdentifier();
        this.onStack = false;
        this.baseAddrReg = base;
        this.isGlobal = true;
        this.function = rvFunction;
    }

    public void setBaseAddrReg(VirtualReg baseAddrReg) {
        this.baseAddrReg = baseAddrReg;
    }

    public VirtualReg getBaseAddrReg() {
        return baseAddrReg;
    }

    public String toString() {
        if (onStack)
            return (this.offset - function.deltaStack) + "(sp)";
        else if (isGlobal)
            return "%lo(" + this.identifier + ")(" + this.baseAddrReg.toString() + ")";
        else
            return this.offset + "(" + this.baseAddrReg.toString() + ")";
    }
}
