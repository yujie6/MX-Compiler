package AST;

import Tools.Location;

public class WhileStmtNode extends StmtNode {
    private ExprNode condition;
    private StmtNode LoopStmt;
    public WhileStmtNode(Location location, ExprNode expr, StmtNode loopStmt) {
        super(location);
        this.condition = expr;
        this.LoopStmt = loopStmt;
    }

    public ExprNode getCondition() {
        return condition;
    }

    public StmtNode getLoopStmt() {
        return LoopStmt;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
