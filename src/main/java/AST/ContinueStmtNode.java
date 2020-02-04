package AST;

import Tools.Location;

public class ContinueStmtNode extends StmtNode{
    public ContinueStmtNode(Location location) {
        super(location);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
