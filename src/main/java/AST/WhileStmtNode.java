package AST;

import Tools.Location;

public class WhileStmtNode extends StmtNode {
    ExprNode expr;
    StmtNode LoopStmt;
    public WhileStmtNode(Location location, ExprNode expr, StmtNode loopStmt) {
        super(location);
        this.expr = expr;
        this.LoopStmt = loopStmt;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
