package Target.RVInstructions;

import Target.*;

import java.util.Set;

public class LUI extends RVInstruction {
    private VirtualReg destReg ;
    private RVOperand immediate;

    public LUI(RVBlock rvBlock, VirtualReg rd, RVOperand imm) {
        super(RVOpcode.lui, rvBlock);
        this.destReg = rd;
        this.immediate = imm;
    }

    @Override
    public Set<VirtualReg> getUseRegs() {
        return Set.of();
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
        // never
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString()).append(",\t");
        if (this.immediate instanceof RVGlobal) {
            ans.append("%hi(").append(((RVGlobal) immediate).getIdentifier()).append(")\n");
        } else if (immediate instanceof Immediate){
            ans.append("hi(").append(((Immediate) immediate).getValue()).append(")\n");
        }
        return ans.toString();
    }
}
