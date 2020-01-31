package AST;

import Tools.Location;

public class BlockNode extends ASTNode {
    public BlockNode(Location location) {
        super(location);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

}
