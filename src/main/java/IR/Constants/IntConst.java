package IR.Constants;

import IR.Module;
import IR.Types.IRBaseType;

public class IntConst extends Constant {
    private int ConstValue;
    public IntConst(int value) {
        super(Module.I32);
        this.ConstValue = value;
    }

    @Override
    public String toString() {
        return String.valueOf(ConstValue);
    }

    @Override
    public Object getValue() {
        return String.valueOf(ConstValue);
    }
}
