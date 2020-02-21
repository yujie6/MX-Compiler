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

    public Type getType() {
        if (VarType instanceof  ArrayTypeNode) {
            return new Type(VarType.getType().getBaseType(), ((ArrayTypeNode) VarType).getArrayLevel(),
                    VarType.getType().getName());
        } else {
            return VarType.getType();
        }
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
