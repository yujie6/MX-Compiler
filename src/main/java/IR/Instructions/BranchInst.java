package IR.Instructions;

import IR.BasicBlock;
import IR.Use;
import IR.Value;

public class BranchInst extends Instruction {


    public BranchInst(BasicBlock parent, Value condition, BasicBlock thenBlock, BasicBlock elseBlock) {
        super(parent, InstType.br);
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

    public String getCondLabel() {
        return ((Instruction)getCondition() ).RegisterID;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("br ");
        if (getCondition() != null) {
            assert getElseBlock() != null;
            ans.append(getCondition().getType().toString()).append(" ");
            ans.append(getCondLabel()).append(", label %");
            ans.append(getThenBlock().getLabel()).append(", label %");
            ans.append(getElseBlock().getLabel());
        } else {
            ans.append("label %").append(getThenBlock().getLabel());
        }
        ans.append("\n");
        return ans.toString();
    }
}
