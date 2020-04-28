package IR.Instructions;

import IR.BasicBlock;
import IR.Use;
import IR.Value;

/**
 * Just pseudo instruction used after ssa destruction
 */
public class CopyInst extends Instruction {

    public boolean isParallel;

    public CopyInst(BasicBlock parent, Value dest, Value src, boolean isParallel) {
        super(parent, InstType.copy);
        this.UseList.add(new Use(dest, this));
        this.UseList.add(new Use(src, this));
        this.isParallel = isParallel;
    }


    public void replaceSrc(Value newSrc) {
        Value oldSrc = getSrc();
        oldSrc.UserList.remove(this);
        this.UseList.set(1, new Use(newSrc, this));
    }

    public Value getDest() {
        return this.UseList.get(0).getVal();
    }

    public Value getSrc() {
        return this.UseList.get(1).getVal();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        if (isParallel) {
            ans.append("pcopy ");
        } else {
            ans.append("copy ");
        }
        ans.append(getRightValueLabel(getSrc())).append(" ");
        ans.append(getRightValueLabel(getDest() )).append("\n");
        return ans.toString();
    }
}
