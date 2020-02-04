package AST;

import Tools.Location;

public abstract class StmtNode extends ASTNode {

    // TODO: Change this into interface instead of class to better implemant semantic checker

    public StmtNode(Location location) {
        super(location);
    }

    @Override
    public abstract void accept(ASTVisitor visitor);
}
