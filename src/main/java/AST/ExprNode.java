package AST;

import Tools.Location;

public abstract class ExprNode extends ASTNode {
    protected Type ExprType;
    private boolean isLeftValue;

    public ExprNode(Location location) {
        super(location);
        isLeftValue = false;
    }

    public Type getExprType() {
        return ExprType;
    }

    public void setExprType(Type exprType) {
        ExprType = exprType;
    }

    public abstract Object accept(ASTVisitor visitor);

    public boolean isLeftValue() {
        return isLeftValue;
    }

    public void setLeftValue(boolean leftValue) {
        isLeftValue = leftValue;
    }
}
