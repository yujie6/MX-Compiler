package AST;

import Tools.Location;

public abstract class ASTNode {
    private Location location;

    public ASTNode(Location location) {
        this.location = location;
    }

    public Location GetLocation() {
        return this.location;
    }

    abstract public Object accept(ASTVisitor visitor);
}
