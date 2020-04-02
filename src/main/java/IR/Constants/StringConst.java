package IR.Constants;

import BackEnd.IRBuilder;
import IR.Module;

public class StringConst extends Constant {
    private String constValue;
    private int strSize;
    private String irStringValue;
    public StringConst(String value) {
        super(Module.STRING);
        this.constValue = value;
        this.strSize = 1;
        this.irStringValue = toString();
    }

    public String getConstValue() {
        return constValue;
    }

    @Override
    public String toString() {
        if (irStringValue == null) {
            StringBuilder text = new StringBuilder();
            for (int i = 1; i < constValue.length() - 1; i++) {
                if (constValue.charAt(i) == '\\') {
                    if (constValue.charAt(i + 1) == '\\')
                        text.append("\\5C");
                    else if (constValue.charAt(i + 1) == 'n')
                        text.append("\\0A");
                    else if (constValue.charAt(i + 1) == '\"') {
                        text.append("\\22");
                    } else {
                        IRBuilder.logger.severe("String const invalid");
                        System.exit(1);
                    }
                    i += 1;
                    this.strSize += 1;
                } else {
                    text.append(constValue.charAt(i));
                    this.strSize += 1;
                }
            }
            return "c\"" + text.toString() + "\\00\"";
        } else return irStringValue;
    }

    @Override
    public Object getValue() {
        return constValue;
    }

    public int getStrSize() {
        return strSize;
    }
}
