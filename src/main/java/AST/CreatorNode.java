package AST;

import Tools.Location;

public class CreatorNode extends ASTNode {
    TypeNode type;
    public CreatorNode(Location location, TypeNode typeNode) {
        super(location);
        this.type = typeNode;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
