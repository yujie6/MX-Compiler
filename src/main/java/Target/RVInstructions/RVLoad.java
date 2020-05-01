package Target.RVInstructions;

import IR.BasicBlock;
import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

public class RVLoad extends RVInstruction {


    private VirtualReg destReg;
    private RVOperand srcAddr;

    public RVLoad(RVOpcode opcode, RVBlock rvBlock, VirtualReg dest, RVOperand src) {
        super(opcode, rvBlock);
        this.destReg = dest;
        this.srcAddr = src;
    }

    @Override
    public String toString() {
        return null;
    }
}
