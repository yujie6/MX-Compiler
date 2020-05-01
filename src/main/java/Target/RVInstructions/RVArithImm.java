package Target.RVInstructions;

import Target.Immediate;
import Target.RVBlock;
import Target.RVOperand;
import Target.VirtualReg;

public class RVArithImm extends RVInstruction {

    RVOperand srcReg;
    Immediate imm;
    VirtualReg destReg;

    public RVArithImm(RVOpcode opcode, RVBlock rvBlock, RVOperand src, Immediate imm, VirtualReg destReg) {
        super(opcode, rvBlock);
        this.srcReg = src;
        this.imm = imm;
        this.destReg = destReg;
    }

    public RVArithImm(RVOpcode opcode, RVBlock rvBlock, RVOperand LHS, RVOperand RHS, VirtualReg destReg) {
        super(opcode, rvBlock);
        if (LHS instanceof Immediate) {
            this.srcReg = RHS;
            this.imm = (Immediate) LHS;
        } else if (RHS instanceof Immediate) {
            this.srcReg = LHS;
            this.imm = ((Immediate) RHS);
        }
        this.destReg = destReg;
    }


    @Override
    public String toString() {
        // take care of subi
        return null;
    }
}
