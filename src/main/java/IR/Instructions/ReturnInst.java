package IR.Instructions;

import IR.BasicBlock;
import IR.Function;
import IR.Types.IRBaseType;
import IR.Value;

public class ReturnInst extends Instruction {
    private IRBaseType RetType;
    private Value RetValue;

    public ReturnInst(BasicBlock parent, IRBaseType retType, Value retValue) {
        super(parent);
        this.Opcode = InstType.Return;
        this.RetType = retType;
        this.RetValue = retValue;
    }

    public IRBaseType getRetType() {
        return RetType;
    }

    public Value getRetValue() {
        return RetValue;
    }
}
