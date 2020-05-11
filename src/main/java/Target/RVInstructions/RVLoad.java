package Target.RVInstructions;

import Target.RVAddr;
import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class RVLoad extends RVInstruction {


    private VirtualReg destReg;
    private RVAddr srcAddr;

    public RVLoad(RVOpcode opcode, RVBlock rvBlock, VirtualReg dest, RVAddr src) {
        super(opcode, rvBlock);
        this.destReg = dest;
        this.srcAddr = src;
    }

    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>(List.of(srcAddr.getBaseAddrReg()) );
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>(List.of(destReg));
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString()).append(",\t").append(srcAddr.toString());
        ans.append("\n");
        return ans.toString();
    }
}
