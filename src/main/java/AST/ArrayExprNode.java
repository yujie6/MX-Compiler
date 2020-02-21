package AST;

import Tools.Location;

public class ArrayExprNode extends ExprNode {
    private ExprNode ArrayId, offset;
    public ArrayExprNode(Location location, ExprNode arrayId, ExprNode offset) {
        super(location);
        this.ArrayId = arrayId;
        this.offset = offset;
    }

    public ExprNode getArrayId() {
        return ArrayId;
    }

    public ExprNode getOffset() {
        return offset;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
