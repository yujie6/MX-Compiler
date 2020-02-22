package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;

public class AllocaInst extends Instruction {

    public AllocaInst(BasicBlock block, IRBaseType type) {
        super(block, InstType.alloca);;
        this.type = type;
    }

}
