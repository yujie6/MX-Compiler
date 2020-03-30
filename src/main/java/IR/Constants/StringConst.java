package IR.Constants;

import IR.Module;
import IR.Types.IRBaseType;

public class StringConst extends Constant {
    private String constValue;
    private int StrSize;
    public StringConst(String value) {
        super(Module.STRING);
        this.constValue = value;
        String tmp = constValue.replace("\\n", "");
        int additional_size = constValue.length() - tmp.length();
        this.StrSize = value.length() -  1 - additional_size / 2;
    }

    public String getConstValue() {
        return constValue;
    }

    @Override
    public String toString() {
        String text = constValue;
        // text = text.replace("\\", "\\5C");
        text = text.replace("\\n", "\\0A");
        text = text.replace("\"", "");
        text = text.replace("\0", "\\00");
        return "c\"" + text + "\\00\"";
    }

    @Override
    public Object getValue() {
        return constValue;
    }

    public int getStrSize() {
        return StrSize;
    }
}
