package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;

public class AllocaInst extends Instruction {
    private Register Instance;

    public AllocaInst(BasicBlock block, Register instance, IRBaseType type) {
        super(block);;
        this.type = type;
    }

}
