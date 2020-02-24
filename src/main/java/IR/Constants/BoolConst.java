package IR.Constants;

public class BoolConst extends Constant {
    private boolean constValue;
    public BoolConst (boolean value) {
        super(IR.Module.I1);
        this.constValue = value;
    }

    public boolean isConstValue() {
        return constValue;
    }
}
