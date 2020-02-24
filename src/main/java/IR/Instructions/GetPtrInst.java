package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;

public class GetPtrInst extends Instruction {
    public GetPtrInst(BasicBlock parent, IRBaseType type) {
        super(parent, InstType.getelementptr);
        this.type = type;

    }

    @Override
    public String toString() {
        return null;
    }
}
