package Target.RVInstructions;

import Target.RVBlock;
import Target.RVOperand;

public class RVStore extends RVInstruction {

    RVOperand destAddr, src;

    public RVStore(RVOpcode opcode, RVBlock rvBlock, RVOperand src, RVOperand dest) {
        super(opcode, rvBlock);
        this.src = src;
        this.destAddr = dest;
    }

    @Override
    public String toString() {
        return null;
    }
}
