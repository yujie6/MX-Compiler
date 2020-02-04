package AST;

import Tools.Location;

public class ExprStmtNode extends StmtNode {
    ExprNode expr;
    public ExprStmtNode(Location location, ExprNode expr) {
        super(location);
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
