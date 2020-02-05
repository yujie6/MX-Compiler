package AST;

import Tools.Location;

import java.util.List;

public class CallExprNode extends ExprNode {
    ExprNode obj;
    List<ExprNode> Parameters;
    public CallExprNode(Location location, ExprNode obj, List<ExprNode> para) {
        super(location);
        this.obj = obj;
        this.Parameters = para;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
