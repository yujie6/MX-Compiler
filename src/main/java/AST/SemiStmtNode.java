package AST;

import Tools.Location;

public class SemiStmtNode extends StmtNode {
    public SemiStmtNode(Location location) {
        super(location);
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
