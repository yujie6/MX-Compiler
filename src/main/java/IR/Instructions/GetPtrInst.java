package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;

public class GetPtrInst extends Instruction {
    public GetPtrInst(BasicBlock parent, IRBaseType type) {
        super(parent, InstType.GetPtr);
        this.type = type;

    }
}
