package AST;

import Tools.Location;

public class ForStmtNode extends StmtNode {
    ExprNode initExpr, condExpr, updateExpr;
    StmtNode LoopStmt;
    public ForStmtNode(Location location, ExprNode initExpr,
                       ExprNode condExpr, ExprNode updateExpr,
                       StmtNode loopStmt) {
        super(location);
        this.initExpr = initExpr;
        this.condExpr = condExpr;
        this.updateExpr = updateExpr;
        this.LoopStmt = loopStmt;
    }
}
