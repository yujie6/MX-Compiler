package IR.Constants;

import IR.Types.IRBaseType;

public class IntConst extends Constant {
    private int ConstValue;
    public IntConst(IRBaseType type, int value) {
        this.type = type;
        this.ConstValue = value;
    }

    public int getConstValue() {
        return ConstValue;
    }
}
