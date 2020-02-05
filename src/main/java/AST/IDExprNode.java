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
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
