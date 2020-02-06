package AST;

import Tools.Location;

import java.util.List;

public class CallExprNode extends ExprNode {
    private ExprNode obj;
    private List<ExprNode> Parameters;
    public CallExprNode(Location location, ExprNode obj, List<ExprNode> para) {
        super(location);
        this.obj = obj;
        this.Parameters = para;
    }

    public ExprNode getObj() {
        return obj;
    }

    public List<ExprNode> getParameters() {
        return Parameters;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
