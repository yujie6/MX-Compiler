package Target.RVInstructions;

import Target.*;

import java.util.ArrayList;
import java.util.List;

public class RVArithImm extends RVInstruction {

    RVOperand srcReg, imm;
    VirtualReg destReg;

    public RVArithImm(RVOpcode opcode, RVBlock rvBlock, RVOperand src, Immediate imm, VirtualReg destReg) {
        super(opcode, rvBlock);
        this.srcReg = src;
        this.imm = imm;
        this.destReg = destReg;
    }

    public RVArithImm(RVOpcode opcode, RVBlock rvBlock, RVOperand LHS, RVOperand RHS, VirtualReg destReg) {
        super(opcode, rvBlock);
        if (LHS instanceof Immediate || LHS instanceof RVGlobal) {
            this.srcReg = RHS;
            this.imm = LHS;
        } else if (RHS instanceof Immediate || RHS instanceof RVGlobal) {
            this.srcReg = LHS;
            this.imm = RHS;
        }
        this.destReg = destReg;
    }


    @Override
    public ArrayList<VirtualReg> getUseRegs() {
        return new ArrayList<>(List.of( (VirtualReg) srcReg));
    }

    @Override
    public ArrayList<VirtualReg> getDefRegs() {
        return new ArrayList<>(List.of(destReg));
    }

    @Override
    public void replaceDef(VirtualReg t) {
        this.destReg = t;
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        if (srcReg == old) {
            srcReg = replaceVal;
        } else {
            // fail
        }
    }


    @Override
    public String toString() {
        // take care of subi
        StringBuilder ans = new StringBuilder(getOpcode());
        ans.append("\t").append(destReg.toString()).append(",\t").append(srcReg.toString());
        ans.append(",\t").append(getImmediate(imm)).append("\n");
        return ans.toString();
    }
}
