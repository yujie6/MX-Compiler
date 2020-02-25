package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;

public class ReturnInst extends Instruction {
    private IRBaseType RetType;

    public ReturnInst(BasicBlock parent, IRBaseType retType, Value retValue) {
        super(parent, InstType.ret);
        this.Opcode = InstType.ret;
        this.RetType = retType;
        this.UseList.add(new Use(retValue, this));
    }

    public IRBaseType getRetType() {
        return RetType;
    }

    public Value getRetValue() {
        return UseList.get(0).getVal();
    }

    private String getRetRegID() {
        return ((Instruction)getRetValue()).RegisterID;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("ret ");
        ans.append(getRetType().toString()).append(" ");
        if (!getRetType().toString().equals("void")) {
            ans.append(getRetRegID());
        }
        return ans.toString();
    }
}
