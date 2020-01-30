package AST;

import Tools.Location;

public class FunctionDecNode extends DecNode {

    public FunctionDecNode(Location location) {
        super(location);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
