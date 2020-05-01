package IR.Instructions;

import BackEnd.IRBuilder;
import IR.BasicBlock;
import IR.IRVisitor;
import IR.Types.IRBaseType;
import IR.Types.PointerType;
import IR.Use;
import IR.Value;

public class LoadInst extends Instruction {


    public LoadInst(BasicBlock parent, IRBaseType type, Value addr) {
        super(parent, InstType.load);
        this.type = type;
        if (type == null) {
            System.exit(1);
        }
        if (! (addr.getType() instanceof PointerType || addr instanceof GetPtrInst) ) {
            IRBuilder.logger.severe("Fatal error: load addr must be a pointer!");
            System.exit(1);
        }
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
        ans.append(  getRightValueLabel(getLoadAddr()) );
        ans.append(", align 8\n");
        return ans.toString();
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
