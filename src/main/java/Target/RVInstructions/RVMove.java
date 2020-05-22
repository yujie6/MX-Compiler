package Target.RVInstructions;

import Target.Immediate;
import Target.RVBlock;
import Target.VirtualReg;

import java.util.ArrayList;
import java.util.Set;

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
    public Set<VirtualReg> getUseRegs() {
        return super.getUseRegs();
    }

    @Override
    public Set<VirtualReg> getDefRegs() {
        return super.getDefRegs();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("mv\t");
        ans.append(getDest().toString()).append(", ");
        ans.append(getSrc().toString()).append("\n");
        return ans.toString();
    }

    public String toAddi() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString()).append(",\t");
        ans.append(srcReg.toString()).append(",\t");
        ans.append(this.imm.toString()).append("\n");
        return ans.toString();
    }
}
