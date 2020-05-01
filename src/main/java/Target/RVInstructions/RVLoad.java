package Target.RVInstructions;

import IR.BasicBlock;
import Target.RVAddr;
import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

import java.util.ArrayList;
import java.util.List;

public class RVLoad extends RVInstruction {


    private VirtualReg destReg;
    private RVOperand srcAddr;

    public RVLoad(RVOpcode opcode, RVBlock rvBlock, VirtualReg dest, RVOperand src) {
        super(opcode, rvBlock);
        this.destReg = dest;
        this.srcAddr = src;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        if (srcAddr instanceof RVAddr) return new ArrayList<>();
        return new ArrayList<>();
    }

    @Override
    public VirtualReg getDefReg() {
        return destReg;
    }

    @Override
    public String toString() {
        return null;
    }
}
