package AST;

import Tools.Location;

public class VariableDecNode extends DecNode {

    TypeNode VarType;
    ExprNode InitValue;

    public VariableDecNode(Location location, String name, TypeNode varType, ExprNode expr) {
        super(location);
        this.VarType = varType;
        this.InitValue = expr;
        this.name = name;
    }

    public ExprNode getInitValue() {
        return InitValue;
    }

    public TypeNode getVarType() {
        return VarType;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
