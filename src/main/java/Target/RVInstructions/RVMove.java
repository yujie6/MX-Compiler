package Target.RVInstructions;

import Target.Immediate;
import Target.RVBlock;
import Target.VirtualReg;

import java.util.ArrayList;

public class RVMove extends RVArithImm {

    static final private Immediate ZERO = new Immediate(0);


    public RVMove(RVBlock rvBlock, VirtualReg src, VirtualReg destReg) {
        super(RVOpcode.addi, rvBlock, src, ZERO, destReg);
    }

    public VirtualReg getSrc() {
        return (VirtualReg) this.srcReg;
    }

    public VirtualReg getDest() {
        return this.destReg;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return super.getUseRegs();
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return super.getDefRegs();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
