package Target.RVInstructions;

import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

import java.util.ArrayList;
import java.util.List;

public class RVStore extends RVInstruction {

    VirtualReg destAddr, src;

    public RVStore(RVOpcode opcode, RVBlock rvBlock, RVOperand src, RVOperand dest) {
        super(opcode, rvBlock);
        this.src = (VirtualReg) src;
        this.destAddr = (VirtualReg) dest;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>(List.of(src, destAddr));
    }

    @Override
    public VirtualReg getDefReg() {
        return null;
    }

    @Override
    public String toString() {
        return null;
    }
}
