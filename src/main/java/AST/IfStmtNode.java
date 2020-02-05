package AST;

import Tools.Location;

import java.util.HashMap;

public class IfStmtNode extends StmtNode{
    private ExprNode expr;
    private StmtNode thenStmt, elseStmt;
    private boolean HasElse;

    public IfStmtNode(Location location, ExprNode expr, StmtNode thenStmt, StmtNode elseStmt, boolean hasElse) {
        super(location);
        this.expr = expr;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
        this.HasElse = hasElse;
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
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
