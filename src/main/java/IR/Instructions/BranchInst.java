package IR.Instructions;

import IR.BasicBlock;
import IR.Use;
import IR.Value;

public class BranchInst extends Instruction {


    public BranchInst(BasicBlock parent, Value condition, BasicBlock thenBlock, BasicBlock elseBlock) {
        super(parent, InstType.Branch);
        this.UseList.add(new Use(condition, this));
        this.UseList.add(new Use(thenBlock, this));
        this.UseList.add(new Use(elseBlock, this));
        this.type = null; // shall change to label type;
    }

    public BasicBlock getThenBlock() {
        return (BasicBlock) this.UseList.get(1).getVal();
    }

    public BasicBlock getElseBlock() {

        return (BasicBlock) this.UseList.get(2).getVal();
    }

    public Value getCondition() {
        return this.UseList.get(0).getVal();
    }
}
