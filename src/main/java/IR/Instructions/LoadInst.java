package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Value;

public class LoadInst extends Instruction {

    private Value LoadValue;
    private Register LoadDestReg;

    public LoadInst(BasicBlock parent, IRBaseType type, Value loadValue, Register loadDestReg) {
        super(parent);
        this.type = type;
        this.LoadDestReg = loadDestReg;
        this.LoadValue = loadValue;
    }


    public Value getLoadValue() {
        return LoadValue;
    }

    public Register getLoadDestReg() {
        return LoadDestReg;
    }
}
