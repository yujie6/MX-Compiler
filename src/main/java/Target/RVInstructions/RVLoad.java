package Target.RVInstructions;

import Target.RVAddr;
import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;
import java.util.Set;

public class RVLoad extends RVInstruction {


    private VirtualReg destReg;
    private RVAddr srcAddr;

    public RVLoad(RVOpcode opcode, RVBlock rvBlock, VirtualReg dest, RVAddr src) {
        super(opcode, rvBlock);
        this.destReg = dest;
        this.srcAddr = src;
    }

    public RVLoad(RVBlock rvBlock, VirtualReg dest, RVAddr src) {
        super(RVOpcode.lw, rvBlock);
        this.destReg = dest;
        this.srcAddr = src;
    }

    @Override
    public Set<VirtualReg> getUseRegs() {
        return Set.of(srcAddr.getBaseAddrReg()) ;
    }

    @Override
    public Set<VirtualReg> getDefRegs() {
        return Set.of(destReg);
    }

    @Override
    public void replaceDef(VirtualReg t) {
        this.destReg = t;
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        if (srcAddr.getBaseAddrReg().equals(old)) {
            srcAddr.setBaseAddrReg(replaceVal);
        }
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString()).append(",\t").append(srcAddr.toString());
        ans.append("\n");
        return ans.toString();
    }
}
