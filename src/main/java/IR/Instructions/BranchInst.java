package IR.Instructions;

import BackEnd.IRBuilder;
import IR.BasicBlock;
import IR.IRVisitor;
import IR.Use;
import IR.Value;

public class BranchInst extends Instruction {

    private boolean hasElse;

    public BranchInst(BasicBlock parent, Value condition, BasicBlock thenBlock, BasicBlock elseBlock) {
        super(parent, InstType.br);
        this.hasElse = false;
        if (condition != null) {
            this.hasElse = true;
            this.UseList.add(new Use(condition, this));
        }
        this.UseList.add(new Use(thenBlock, this));
        if (elseBlock != null)
            this.UseList.add(new Use(elseBlock, this));
        parent.addSuccessor(thenBlock);
        thenBlock.addPredecessor(parent);
        if (elseBlock != null) {
            parent.addSuccessor(elseBlock);
            elseBlock.addPredecessor(parent);
        }
        this.type = null; // shall change to label type;
    }

    public boolean isHasElse() {
        return hasElse;
    }

    public BasicBlock getThenBlock() {
        if (hasElse) return (BasicBlock) this.UseList.get(1).getVal();
        return (BasicBlock) this.UseList.get(0).getVal();
    }

    public BasicBlock getElseBlock() {

        return (BasicBlock) this.UseList.get(2).getVal();
    }

    public void replaceSuccBlock(BasicBlock original, BasicBlock replaceBB) {
        BasicBlock fa = this.Parent;
        if (fa.successors.contains(original)) {
            fa.successors.remove(original);
            fa.successors.add(replaceBB);
            original.predecessors.remove(fa);
            replaceBB.predecessors.add(fa);

        } else System.exit(2);

        if (hasElse) {
            if (getThenBlock() == original) {
                this.UseList.set(1, new Use(replaceBB, this));
            } else if (getElseBlock() == original) {
                this.UseList.set(2, new Use(replaceBB, this));
            } else System.exit(3);
        } else {
            this.UseList.set(0, new Use(replaceBB, this));
        }

    }

    public Value getCondition() {
        if (!hasElse) return null;
        return this.UseList.get(0).getVal();
    }

    public String getCondLabel() {
        return ((Instruction) getCondition()).RegisterID;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("br ");
        if (hasElse) {
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

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
