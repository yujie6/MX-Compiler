package IR.Constants;

import IR.Module;
import IR.Types.IRBaseType;

public class StringConst extends Constant {
    private String constValue;
    private int StrSize;
    public StringConst(String value) {
        super(Module.STRING);
        this.constValue = value;
        this.StrSize = value.length() -  2;
    }

    public String getConstValue() {
        return constValue;
    }

    @Override
    public String toString() {
        String text = constValue;
        text = text.replace("\\", "\\5C");
        text = text.replace("\n", "\\0A");
        text = text.replace("\"", "");
        text = text.replace("\0", "\\00");

        return "c\"" + text + "\"";
    }

    @Override
    public Object getValue() {
        return constValue;
    }

    public int getStrSize() {
        return StrSize;
    }
}
