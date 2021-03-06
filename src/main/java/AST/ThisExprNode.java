package AST;

import Tools.Location;

public class ThisExprNode extends ExprNode {
    public ThisExprNode(Location location) {
        super(location);
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
