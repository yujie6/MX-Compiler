package IR.Constants;

public class BoolConst extends Constant {
    private int constValue;
    public BoolConst (boolean value) {
        super(IR.Module.I1);
        this.constValue = value ? 1 : 0;
    }

    @Override
    public Object getValue() {
        return String.valueOf(constValue);
    }
}
