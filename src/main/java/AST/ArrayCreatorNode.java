package AST;

import Tools.Location;

import java.util.List;

public class ArrayCreatorNode extends CreatorNode {

    private int ArrayLevel;
    private List<ExprNode> ExprList;

    public ArrayCreatorNode(Location location, TypeNode type,
                            List<ExprNode> exprList, int arrayLevel) {
        super(location, type);
        this.ExprList = exprList;
        this.ArrayLevel = arrayLevel;
        this.ExprType = type.getType();
    }

    public int getArrayLevel() {
        return ArrayLevel;
    }

    public List<ExprNode> getExprList() {
        return ExprList;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
