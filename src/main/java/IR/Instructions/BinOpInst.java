package IR.Instructions;


import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;
import Tools.Operators;

public class BinOpInst extends Instruction {

    public BinOpInst(BasicBlock parent, IRBaseType type, InstType opcode, Value LHS, Value RHS) {
        super(parent, opcode);;
        this.type = type;
        this.UseList.add(new Use(LHS, this));
        this.UseList.add(new Use(RHS, this));
    }

    public Value getLHS() {
        return UseList.get(0).getVal();
    }

    public Value getRHS() {
        return UseList.get(1).getVal();
    }
}
