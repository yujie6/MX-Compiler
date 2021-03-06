package AST;

import Tools.Location;

public class IDExprNode extends ExprNode {
    private String Identifier;
    public IDExprNode(Location location, String id) {
        super(location);
        this.Identifier = id;
    }

    public String getIdentifier() {
        return Identifier;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        return visitor.visit(this);
    }
}
