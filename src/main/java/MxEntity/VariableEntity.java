package MxEntity;

import AST.ParameterNode;
import AST.Type;

public class VariableEntity extends Entity {
    Type VarType;

    public VariableEntity(String id, Type varType) {
        super(id);
        setScope(null);
        this.VarType = varType;
    }

    public VariableEntity(ParameterNode para) {
        super(para.getIdentifier());
        setScope(null);
        this.VarType = para.getType();
    }

    public Type getVarType() {
        return VarType;
    }

}
