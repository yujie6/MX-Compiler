package AST;

import Tools.Location;

public class ReturnStmtNode extends StmtNode {
    private ExprNode ReturnedExpr;
    public ReturnStmtNode(Location location, ExprNode expr) {
        super(location);
        this.ReturnedExpr = expr;
    }

    public Type getRetType() {
        if (ReturnedExpr != null) {
            return ReturnedExpr.getExprType();
        } else {
            return new Type(BaseType.RTYPE_VOID);
        }
    }

    public ExprNode getReturnedExpr() {
        return ReturnedExpr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
