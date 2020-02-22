package IR.Instructions;


import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;
import Tools.Operators;

public class BinOpInst extends Instruction {
    private Operators.BinaryOp bop;

    public BinOpInst(BasicBlock parent, IRBaseType type, Operators.BinaryOp op, Value LHS, Value RHS) {
        super(parent, InstType.BinOp);;
        this.bop = op;
        this.type = type;
        this.UseList.add(new Use(LHS, this));
        this.UseList.add(new Use(RHS, this));
    }

    public Operators.BinaryOp getBop() {
        return bop;
    }

    public Value getLHS() {
        return UseList.get(0).getVal();
    }

    public Value getRHS() {
        return UseList.get(1).getVal();
    }
}
