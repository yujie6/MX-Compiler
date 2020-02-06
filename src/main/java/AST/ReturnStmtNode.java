package AST;

import Tools.Location;

public class ReturnStmtNode extends StmtNode {
    private ExprNode ReturnedExpr;
    public ReturnStmtNode(Location location, ExprNode expr) {
        super(location);
        this.ReturnedExpr = expr;
    }

    public ExprNode getReturnedExpr() {
        return ReturnedExpr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
