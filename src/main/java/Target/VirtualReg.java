package Target;

import IR.Instructions.AllocaInst;
import IR.Instructions.Instruction;

public class VirtualReg extends RVOperand {

    String identifier;


    public VirtualReg(Instruction inst) {
        this.identifier = inst.getRightValueLabel(inst);
    }

    public VirtualReg(String name) {
        this.identifier = name;
    }

    public String getIdentifier() {
        return identifier;
    }
}
