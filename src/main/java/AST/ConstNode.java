package AST;

import Tools.Location;

public class ConstNode extends ExprNode {
    Type type;

    public ConstNode(Location location, Type type) {
        super(location);
        this.type = type;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}