package AST;

import Tools.Location;

public class StmtNode extends ASTNode {

    public StmtNode(Location location) {
        super(location);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
