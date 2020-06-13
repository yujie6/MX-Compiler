package Target.RVInstructions;

import Target.*;

import java.util.Set;

public class RVLoad extends RVInstruction {


    private VirtualReg destReg;
    private RVAddr srcAddr;
    private RVGlobal gvar;

    public RVLoad(RVBlock rvBlock, VirtualReg dest, RVAddr src) {
        super(RVOpcode.lw, rvBlock);
        this.destReg = dest;
        this.srcAddr = src;
    }

    public RVLoad(RVBlock rvBlock, VirtualReg dest, RVGlobal gvar) {
        super(RVOpcode.lw, rvBlock);
        this.destReg = dest;
        this.gvar = gvar;
    }

    public RVOperand getAddr() {
        if (gvar != null ) return gvar;
        return srcAddr;
    }

    public VirtualReg getDestReg() {
        return destReg;
    }

    @Override
    public Set<VirtualReg> getUseRegs() {
        if (gvar != null) return Set.of();
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
        if (srcAddr == null) return;
        if (srcAddr.getBaseAddrReg().equals(old)) {
            srcAddr.setBaseAddrReg(replaceVal);
        }
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString()).append(",\t");
        if (gvar != null) {
            ans.append(gvar.getIdentifier());
        } else ans.append(srcAddr.toString());
        ans.append("\n");
        return ans.toString();
    }
}
