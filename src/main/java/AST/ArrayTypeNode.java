package AST;

import Tools.Location;

public class ArrayTypeNode extends TypeNode {
    private TypeNode OriginalType;
    private int ArrayLevel;
    public ArrayTypeNode(TypeNode originalType, int arrayLevel) {
        super(originalType.GetLocation(), originalType.getType());
        this.ArrayLevel = arrayLevel;
    }

    public TypeNode getOriginalType() {
        return OriginalType;
    }

    public int getArrayLevel() {
        return ArrayLevel;
    }
}
