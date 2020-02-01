package AST;

import Tools.Location;

public class MemberExprNode extends ExprNode {
    private ExprNode expr;
    private String member;
    public MemberExprNode(Location location, ExprNode expr, String id) {
        super(location);
        this.expr = expr;
        this.member = id;
    }
}
