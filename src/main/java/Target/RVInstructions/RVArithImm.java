package Target.RVInstructions;

import Target.*;
import java.util.Set;

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
    public Set<VirtualReg> getUseRegs() {
        if (!(srcReg instanceof VirtualReg) ) {

        }
        return Set.of( (VirtualReg) srcReg);
    }

    @Override
    public Set<VirtualReg>getDefRegs() {
        return Set.of(destReg);
    }

    @Override
    public void replaceDef(VirtualReg t) {
        this.destReg = t;
    }

    @Override
    public void replaceUse(VirtualReg old, VirtualReg replaceVal) {
        if (srcReg .equals(old)) {
            srcReg = replaceVal;
        } else {
            // fail
            System.out.println("This should never happen(RVArithImm replaceUse)");
        }
    }

    public void setImm(Immediate imm1) {
        this.imm = imm1;
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
