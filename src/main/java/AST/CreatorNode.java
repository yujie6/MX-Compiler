package AST;

import Tools.Location;

public abstract class CreatorNode extends ExprNode {
    TypeNode type;

    public CreatorNode(Location location, TypeNode typeNode) {
        super(location);
        this.type = typeNode;
    }
}
