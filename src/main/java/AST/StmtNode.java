package AST;

import Tools.Location;

public abstract class StmtNode extends ASTNode {

    public StmtNode(Location location) {
        super(location);
    }

    @Override
    public abstract void accept(ASTVisitor visitor);
}
