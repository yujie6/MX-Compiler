package AST;

import Tools.Location;

public abstract class DecNode extends ASTNode {

    protected String identifier;

    public DecNode(Location location) {
        super(location);
    }

    public String getIdentifier() {
        return identifier;
    }

    public abstract Object accept(ASTVisitor visitor);

}
