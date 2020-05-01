package Target;

import IR.BasicBlock;
import Target.RVInstructions.RVInstruction;

import java.util.LinkedList;

public class RVBlock {

    private BasicBlock irBlock;
    public LinkedList<RVInstruction> rvInstList;

    public RVBlock(BasicBlock block) {
        this.irBlock = block;
        this.rvInstList = new LinkedList<>();
    }

    public void AddInst(RVInstruction inst) {
        rvInstList.add(inst);
    }


}
