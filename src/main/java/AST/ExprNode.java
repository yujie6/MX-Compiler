package AST;

import Tools.Location;

public class ExprNode extends ASTNode {

    public ExprNode(Location location) {
        super(location);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
