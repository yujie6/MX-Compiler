package IR.Constants;

import IR.Types.IRBaseType;

public class NullConst extends Constant {

    public NullConst(IRBaseType type) {
        super(type);
    }

    @Override
    public void setType(IRBaseType type) {
        super.setType(type);
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "null";
    }
}
