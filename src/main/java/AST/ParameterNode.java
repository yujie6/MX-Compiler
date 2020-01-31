package AST;

import Tools.Location;

public class ParameterNode extends DecNode {
    TypeNode VarType;

    public ParameterNode(Location location, TypeNode vartype, String id) {
        super(location);
        this.VarType = vartype;
        this.identifier = id;
    }

    public TypeNode getVarType() {
        return VarType;
    }


}
