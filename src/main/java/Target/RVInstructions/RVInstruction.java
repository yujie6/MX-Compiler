package Target.RVInstructions;

import Target.*;

import java.util.ArrayList;

public abstract class RVInstruction {
    private RVOpcode opcode;
    private RVBlock parentBB;

    public RVInstruction(RVOpcode opcode, RVBlock rvBlock) {
        this.opcode = opcode;
        this.parentBB = rvBlock;
    }

    public abstract ArrayList<VirtualReg> getUseRegs();
    public abstract ArrayList<VirtualReg> getDefRegs();
    public abstract void replaceDef(VirtualReg t);
    public abstract void replaceUse(VirtualReg old, VirtualReg replaceVal);

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

    public String getImmediate(RVOperand imm) {
        if (imm instanceof Immediate) {
            return imm.toString();
        } else if (imm instanceof RVGlobal) {
            return "%lo(" + ((RVGlobal) imm).getIdentifier() + ")";
        } else {
            return null;
        }
    }

    public void insertAfterMe(RVInstruction other) {
        int index = parentBB.rvInstList.indexOf(this);
        parentBB.rvInstList.add(index + 1, other);
    }

    public void insertBeforeMe(RVInstruction other) {
        int index = parentBB.rvInstList.indexOf(this);
        parentBB.rvInstList.add(index, other);
    }

    public RVBlock getParentBB() {
        return parentBB;
    }

    public abstract String toString();
}
