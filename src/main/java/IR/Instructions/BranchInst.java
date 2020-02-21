package IR.Instructions;

import IR.BasicBlock;
import IR.Value;

public class BranchInst extends Instruction {
    private Value Condition;
    private BasicBlock ThenBlock, ElseBlock;


    public BranchInst(BasicBlock parent, Value condition, BasicBlock thenBlock, BasicBlock elseBlock) {
        super(parent);
        this.Condition = condition;
        this.ThenBlock = thenBlock;
        this.ElseBlock = elseBlock;
    }

    public BasicBlock getThenBlock() {
        return ThenBlock;
    }

    public BasicBlock getElseBlock() {
        return ElseBlock;
    }

    public Value getCondition() {
        return Condition;
    }
}
