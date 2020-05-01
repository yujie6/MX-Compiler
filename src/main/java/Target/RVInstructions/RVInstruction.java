package Target.RVInstructions;

import Target.RVBlock;
import Target.VirtualReg;

import java.util.ArrayList;

public abstract class RVInstruction {
    private RVOpcode opcode;
    private RVBlock parentBB;

    public RVInstruction(RVOpcode opcode, RVBlock rvBlock) {
        this.opcode = opcode;
        this.parentBB = rvBlock;
    }

    public abstract ArrayList<VirtualReg> getUseRegs();
    public abstract VirtualReg getDefReg();

    public boolean isBranch() {
        return opcode.equals(RVOpcode.bne) || opcode.equals(RVOpcode.beq) ||
                opcode.equals(RVOpcode.bge) || opcode.equals(RVOpcode.bgeu) ||
                opcode.equals(RVOpcode.blt) || opcode.equals(RVOpcode.bltu);
    }

    public boolean isShifting() {
        return false;
    }

    public String getOpcode() {
        return opcode.toString();
    }

    public abstract String toString();
}
