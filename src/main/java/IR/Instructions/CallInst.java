package IR.Instructions;

import IR.BasicBlock;
import IR.Function;

public class CallInst extends Instruction {

    private Function func;

    public CallInst(BasicBlock parent) {
        super(parent);
    }
}
