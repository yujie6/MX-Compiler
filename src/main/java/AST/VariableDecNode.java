package AST;

import Tools.Location;
import Tools.MXError;

import java.util.List;

public class VariableDecNode extends DecNode {

    private TypeNode VarType;
    private boolean global;
    private List<VarDecoratorNode> VarDecoratorList;

    public VariableDecNode(Location location,
                           TypeNode varType,
                           List<VarDecoratorNode> varDecoratorList) {
        super(location);
        if (varType.getType().getBaseType() == BaseType.RTYPE_VOID) {
            throw new MXError("Variable can not be declared as void type.", location);
        }
        this.VarType = varType;
        this.VarDecoratorList = varDecoratorList;
    }

    public List<VarDecoratorNode> getVarDecoratorList() {
        return VarDecoratorList;
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

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }
}
