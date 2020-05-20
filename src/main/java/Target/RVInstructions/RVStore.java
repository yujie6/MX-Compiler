package Target.RVInstructions;

import Target.RVAddr;
import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

import java.util.ArrayList;
import java.util.List;

public class RVStore extends RVInstruction {

    VirtualReg src;
    RVAddr destAddr;

    public RVStore(RVOpcode opcode, RVBlock rvBlock, RVOperand src, RVOperand dest) {
        super(opcode, rvBlock);
        this.src = (VirtualReg) src;
        this.destAddr = (RVAddr) dest;
    }

    public RVStore(RVBlock rvBlock, RVOperand src, RVOperand dest) {
        super(RVOpcode.sw, rvBlock);
        this.src = (VirtualReg) src;
        this.destAddr = (RVAddr) dest;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>(List.of(src, destAddr.getBaseAddrReg()));
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>();
    }

    @Override
    public void replaceDef(VirtualReg t) {
        // never accessed
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        if (old == src) {
            src = replaceVal;
        } else {
            // fail;
        }
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("sw");
        ans.append("\t").append(src.toString()).append(",\t");
        ans.append(destAddr.toString()).append("\n");
        return ans.toString();
    }
}
