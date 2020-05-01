package IR.Instructions;

import IR.BasicBlock;
import IR.Constants.IntConst;
import IR.Constants.NullConst;
import IR.IRVisitor;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;

import java.util.ArrayList;

public class PhiInst extends Instruction {

    private ArrayList<BasicBlock> blockList;
    private int branchNum;

    public PhiInst(BasicBlock parent, IRBaseType type) {
        super(parent, InstType.phi);
        this.blockList = new ArrayList<>();
        this.type = type;
        this.branchNum = 0;
    }

    public void AddPhiBranch(BasicBlock basicBlock, Value var) {
        if (var == null) {
            if (this.type.isIntegerType()) {
                var = new IntConst(0);
            } else if (this.type.isPointerType()) {
                var = new NullConst(this.type);
            }
        }
        this.UseList.add(new Use(var, this));
        this.blockList.add(basicBlock);
        this.branchNum += 1;
    }

    public void removeBranch(int index) {
        blockList.remove(index);
        this.UseList.remove(index);
        this.branchNum -= 1;
    }

    public int getBranchNum() {
        return branchNum;
    }

    public BasicBlock getBlock(int index) {
        return blockList.get(index);
    }

    public Value getValue(int index) {
        return this.UseList.get(index).getVal();
    }


    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getRegisterID());
        ans.append(" = phi ").append(this.type.toString()).append(" ");
        for (int i = 0; i < blockList.size(); i++) {
            ans.append("[ ").append(getRightValueLabel(getValue(i)));
            ans.append(", %").append(getBlock(i).getLabel());
            ans.append(" ] ");
            if (i != blockList.size() - 1) {
                ans.append(", ");
            }
        }
        ans.append("\n");
        return ans.toString();
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
