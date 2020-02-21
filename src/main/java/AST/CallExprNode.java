package AST;

import MxEntity.FunctionEntity;
import Tools.Location;

import java.util.List;

public class CallExprNode extends ExprNode {
    private ExprNode obj;
    private List<ExprNode> Parameters;
    private FunctionEntity function;
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

    public void setFunction(FunctionEntity function) {
        this.function = function;
    }

    public FunctionEntity getFunction() {
        return function;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
