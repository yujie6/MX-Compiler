package MxEntity;

import AST.ParameterNode;
import AST.Type;
import AST.VarDecoratorNode;
import AST.VariableDecNode;
import Frontend.Scope;
import Tools.Location;

public class VariableEntity extends Entity {
    private Type VarType;
    private Location location;

    public VariableEntity(String id, Type varType) {
        super(id);
        setScope(null);
        this.VarType = varType;
    }

    public VariableEntity(Scope scope, ParameterNode para) {
        super(para.getIdentifier());
        setScope(scope);
        this.location = para.GetLocation();
        this.VarType = para.getType();
    }

    public VariableEntity(Scope scope, VarDecoratorNode var, Type decType) {
        super(var.getIdentifier());
        setScope(scope);
        this.location = var.GetLocation();
        this.VarType = decType;
    }

    public Location getLocation() {
        return location;
    }


    public Type getVarType() {
        return VarType;
    }

}
