package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;

public class LoadInst extends Instruction {


    public LoadInst(BasicBlock parent, IRBaseType type, Value addr) {
        super(parent, InstType.load);
        this.type = type;
        this.UseList.add(new Use(addr, this));
    }

    public Value getLoadAddr() {
        return UseList.get(0).getVal();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(RegisterID);
        ans.append(" = ").append("load ").append(type.toString());
        ans.append(", ").append(getLoadAddr().getType().toString()).append(" ");
        ans.append(  ((Instruction) getLoadAddr()).RegisterID );
        ans.append(", align 4\n");
        return ans.toString();
    }
}
