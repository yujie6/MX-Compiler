package IR;

import IR.Types.IRBaseType;

public class GlobalVariable extends Value {

    private String Identifier;
    private Value InitValue;

    public GlobalVariable(IRBaseType type, String id, Value initValue) {
        super(ValueType.GLOBAL_VAR);
        this.type = type;
        this.Identifier = id;
        this.InitValue = initValue;
    }

    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) {

    }

    public String getIdentifier() {
        return Identifier;
    }

    public void setInitValue(Value initValue) {
        InitValue = initValue;
    }

    public Value getInitValue() {
        return InitValue;
    }
}
