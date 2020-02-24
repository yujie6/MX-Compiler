package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Types.PointerType;

public class AllocaInst extends Instruction {

    public AllocaInst(BasicBlock block, IRBaseType type) {
        super(block, InstType.alloca);;
        this.type = new PointerType(type);
    }

}
