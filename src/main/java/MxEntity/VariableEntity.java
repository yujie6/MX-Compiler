package MxEntity;

import AST.ParameterNode;
import AST.Type;
import AST.VariableDecNode;

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

    public VariableEntity(VariableDecNode var) {
        super(var.getIdentifier());
        setScope(null);
        this.VarType = var.getType();

    }

    public Type getVarType() {
        return VarType;
    }

}
