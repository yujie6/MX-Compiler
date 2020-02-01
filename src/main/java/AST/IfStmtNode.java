package AST;

import Tools.Location;

import java.util.HashMap;

public class IfStmtNode extends StmtNode{
    ExprNode expr;
    StmtNode thenStmt, elseStmt;
    boolean HasElse;

    public IfStmtNode(Location location, ExprNode expr, StmtNode thenStmt, StmtNode elseStmt, boolean hasElse) {
        super(location);
        this.expr = expr;
        this.thenStmt = thenStmt;
        this.elseStmt = elseStmt;
        this.HasElse = hasElse;
    }

}
