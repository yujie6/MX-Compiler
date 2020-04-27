package Target;

import IR.BasicBlock;
import Target.RVInstructions.RVInstruction;

import java.util.LinkedList;

public class RVBlock {

    BasicBlock irBlock;
    LinkedList<RVInstruction> rvInstList;

    public RVBlock(BasicBlock block) {
        this.irBlock = block;
        this.rvInstList = new LinkedList<>();
    }


}
