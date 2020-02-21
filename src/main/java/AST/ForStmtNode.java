package AST;

import Tools.Location;

import java.util.ArrayList;

public class ForStmtNode extends StmtNode {
    private ExprNode initExpr, condExpr, updateExpr;
    private StmtNode LoopStmt;
    private BlockNode LoopBlcok;
    public ForStmtNode(Location location, ExprNode initExpr,
                       ExprNode condExpr, ExprNode updateExpr,
                       StmtNode loopStmt) {
        super(location);
        this.initExpr = initExpr;
        this.condExpr = condExpr;
        this.updateExpr = updateExpr;
        this.LoopStmt = loopStmt;
        ArrayList<StmtNode> blockstmt = new ArrayList<StmtNode>();
        blockstmt.add(loopStmt);
        this.LoopBlcok = new BlockNode(loopStmt.GetLocation(), blockstmt);
    }

    public StmtNode getLoopStmt() {
        return LoopStmt;
    }

    public BlockNode getLoopBlcok() {
        return LoopBlcok;
    }

    public ExprNode getCondExpr() {
        return condExpr;
    }

    public ExprNode getInitExpr() {
        return initExpr;
    }

    public ExprNode getUpdateExpr() {
        return updateExpr;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
