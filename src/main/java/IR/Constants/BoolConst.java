package IR.Constants;

public class BoolConst extends Constant {
    public int constValue;
    public BoolConst (boolean value) {
        super(IR.Module.I1); //shall be i8
        this.constValue = value ? 1 : 0;
    }

    @Override
    public String toString() {
        return String.valueOf(constValue);
    }

    @Override
    public Object getValue() {
        return String.valueOf(constValue);
    }
}
