package AST;

import Tools.*;

public class PrefixExprNode extends ExprNode {
    private Operators.PreFixOp PrefixOp;
    private ExprNode Expr;
    public PrefixExprNode(Location location, Operators.PreFixOp preFixOp, ExprNode Expr) {
        super(location);
        this.PrefixOp = preFixOp;
        this.Expr = Expr;
    }

    public ExprNode getExpr() {
        return Expr;
    }

    public Operators.PreFixOp getPrefixOp() {
        return PrefixOp;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
