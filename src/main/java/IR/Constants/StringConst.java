package IR.Constants;

import IR.Module;
import IR.Types.IRBaseType;

public class StringConst extends Constant {
    private String constValue;
    public StringConst(String value) {
        super(Module.STRING);
        this.constValue = value;
    }

    public String getConstValue() {
        return constValue;
    }

    @Override
    public Object getValue() {
        return constValue;
    }
}
