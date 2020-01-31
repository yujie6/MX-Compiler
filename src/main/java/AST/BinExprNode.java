package AST;

import Tools.*;

public class BinExprNode extends ExprNode {
    private Operators.BinaryOp bop;
    private ExprNode LeftExpr, RightExpr;

    public BinExprNode(Location location, Operators.BinaryOp op,
                       ExprNode leftExpr, ExprNode rightExpr) {
        super(location);
        this.bop = op;
        this.LeftExpr = leftExpr;
        this.RightExpr = rightExpr;
    }
}
