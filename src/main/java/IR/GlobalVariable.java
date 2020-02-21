package IR;

import IR.Types.IRBaseType;

public class GlobalVariable extends Value {

    private String Identifier;
    private Value InitValue;

    public GlobalVariable(IRBaseType type, String id, Value initValue) {
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
}
