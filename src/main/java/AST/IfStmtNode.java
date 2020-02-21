package AST;

import Tools.Location;

public class IfStmtNode extends StmtNode{
    private ExprNode ConditionExpr;
    private StmtNode thenStmt, elseStmt;
    private boolean HasElse;

    public IfStmtNode(Location location, ExprNode expr, StmtNode thenStmt, StmtNode elseStmt, boolean hasElse) {
        super(location);
        this.ConditionExpr = expr;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
        this.HasElse = hasElse;
    }

    public ExprNode getConditionExpr() {
        return ConditionExpr;
    }

    public StmtNode getThenStmt() {
        return thenStmt;
    }

    public StmtNode getElseStmt() {
        return elseStmt;
    }

    public boolean isHasElse() {
        return HasElse;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
