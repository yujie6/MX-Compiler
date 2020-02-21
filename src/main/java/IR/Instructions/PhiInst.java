package IR.Instructions;


import IR.BasicBlock;

public class PhiInst extends Instruction {
    public PhiInst(BasicBlock parent) {
        super(parent);
    }
}
