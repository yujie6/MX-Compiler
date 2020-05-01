package Target.RVInstructions;

import Target.RVBlock;

public class RVRet extends RVInstruction {

    public RVRet(RVBlock rvBlock) {
        super(RVOpcode.ret, rvBlock);
    }

    @Override
    public String toString() {
        return "ret\n";
    }
}
