package IR.Instructions;

import IR.BasicBlock;
import IR.IRBaseNode;
import IR.IRMap;
import IR.IRVisitor;
import IR.Types.IRBaseType;
import IR.Types.PointerType;

public class AllocaInst extends Instruction {

    private IRBaseType baseType;

    public AllocaInst(BasicBlock block, IRBaseType type) {
        super(block, InstType.alloca);
        this.type = new PointerType(type);
        baseType = type;
    }

    public IRBaseType getBaseType() {
        return baseType;
    }

    @Override
    public void copyTo(BasicBlock other, IRMap irMap) {
        other.AddInstAtTail(new AllocaInst(other, baseType));
    }

    @Override
    public String toString() {
        String ans = RegisterID + " = " + "alloca ";
        ans += baseType.toString();
        ans += ", align 4\n";
        return ans;
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
