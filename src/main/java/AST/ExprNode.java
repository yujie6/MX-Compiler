package AST;

import Tools.Location;

public abstract class ExprNode extends ASTNode {
    protected Type ExprType;

    public ExprNode(Location location) {
        super(location);
    }

    public Type getExprType() {
        return ExprType;
    }

    public void setExprType(Type exprType) {
        ExprType = exprType;
    }

    public abstract void accept(ASTVisitor visitor);
}
