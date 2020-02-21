package AST;

import Tools.Location;

public class ContinueStmtNode extends StmtNode{
    public ContinueStmtNode(Location location) {
        super(location);
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
