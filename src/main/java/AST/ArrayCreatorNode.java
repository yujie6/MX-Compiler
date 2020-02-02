package AST;

import Tools.Location;

import java.util.List;

public class ArrayCreatorNode extends CreatorNode {

    int ArrayLevel;
    List<ExprNode> ExprList;

    public ArrayCreatorNode(Location location, TypeNode type,
                            List<ExprNode> exprList, int arrayLevel) {
        super(location, type);
        this.ExprList = exprList;
        this.ArrayLevel = arrayLevel;
    }

}
