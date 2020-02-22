package IR.Instructions;

import IR.BasicBlock;
import IR.Use;
import IR.Value;
import Tools.Operators;

public class UnaryOpInst extends Instruction {
    private Operators uop;

    public UnaryOpInst(BasicBlock parent, Value target, Operators op) {
        super(parent, InstType.UnaOp);
        this.uop = op;
        this.UseList.add(new Use(target, this));
    }

    public Value getTheValue() {
        return UseList.get(0).getVal();
    }
}
