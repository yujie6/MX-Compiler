package IR.Instructions;

import IR.IRBaseNode;
import IR.IRVisitor;
import IR.Types.IRBaseType;
import IR.Value;

public class Register extends Value {

    private String name;
    static private int RegNum = 0;
    private Instruction definition;

    public Register(String name, IRBaseType type, Instruction def) {
        this.type = type;
        this.name = name;
        this.definition = def;
    }

    public IRBaseType getType() {
        return this.type;
    }

    public Instruction getDefinition() {
        return definition;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) {

    }
}
