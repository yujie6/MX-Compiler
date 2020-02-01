package AST;

import Tools.Location;

public class ReturnStmtNode extends StmtNode {
    ExprNode expr;
    public ReturnStmtNode(Location location, ExprNode expr) {
        super(location);
        this.expr = expr;
    }
}
