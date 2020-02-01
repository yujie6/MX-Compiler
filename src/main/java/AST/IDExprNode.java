package AST;

import Tools.Location;

public class IDExprNode extends ExprNode {
    String Identifier;
    public IDExprNode(Location location, String id) {
        super(location);
        this.Identifier = id;
    }
}
