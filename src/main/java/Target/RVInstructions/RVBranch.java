package Target.RVInstructions;

import Target.RVBlock;
import Target.RVInstructions.RVOpcode;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RVBranch extends RVInstruction {

    public RVBranch(RVOpcode opcode, RVBlock rvBlock) {
        super(opcode, rvBlock);
    }
}
