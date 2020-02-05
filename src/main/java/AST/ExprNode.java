package AST;

import Tools.Location;

public abstract class ExprNode extends ASTNode {

    public ExprNode(Location location) {
        super(location);
    }

    public abstract void accept(ASTVisitor visitor);
}
