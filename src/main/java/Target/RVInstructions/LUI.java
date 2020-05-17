package Target.RVInstructions;

import Target.*;

import java.util.ArrayList;
import java.util.List;

public class LUI extends RVInstruction {
    private VirtualReg destReg ;
    private RVOperand immediate;

    public LUI(RVBlock rvBlock, VirtualReg rd, RVOperand imm) {
        super(RVOpcode.lui, rvBlock);
        this.destReg = rd;
        this.immediate = imm;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>(List.of(destReg));
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>();
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
        } else {
            ans.append(immediate.toString()).append("\n");
        }
        return ans.toString();
    }
}
