package IR;

import IR.Constants.Constant;
import IR.Types.IRBaseType;
import IR.Types.PointerType;

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

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("@");
        ans.append(Identifier).append(" = ");
        ans.append("dso_local global ").append(type.toString());
        ans.append(" ");
        if (InitValue instanceof Constant) {
            ans.append(InitValue.toString()); // init value shall be const or new expr
        } else {
            ans.append("zeroinitializer");
        }

        ans.append(" , align ");
        int align = (type instanceof PointerType) ? 8 : 4;
        ans.append(String.valueOf(align)).append("\n");
        return ans.toString();
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
