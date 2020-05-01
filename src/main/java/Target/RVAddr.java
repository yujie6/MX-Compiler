package Target;

import IR.Instructions.AllocaInst;

public class RVAddr extends RVOperand {
    String identifier;
    AllocaInst allocaInst;
    int addr;

    public RVAddr(AllocaInst allocaInst, RVFunction rvFunction) {
        this.allocaInst = allocaInst;
        this.identifier = allocaInst.getRightValueLabel(allocaInst);
        this.addr = rvFunction.allocaOnStack();
    }

    public String toString() {
        return this.addr + "(sp)";
    }
}
