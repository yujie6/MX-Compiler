package AST;

import Tools.*;

public class PostfixExprNode extends ExprNode {
    private Operators.PostFixOp PostfixOp;
    private ExprNode Expr;
    public PostfixExprNode(Location location, Operators.PostFixOp op, ExprNode expr) {
        super(location);
        this.PostfixOp = op;
        this.Expr = expr;
    }

    public ExprNode getExpr() {
        return Expr;
    }

    public Operators.PostFixOp getPostfixOp() {
        return PostfixOp;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
