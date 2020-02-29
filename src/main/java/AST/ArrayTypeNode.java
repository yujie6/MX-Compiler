package AST;

import Tools.Location;

public class ArrayTypeNode extends TypeNode {
    // private TypeNode OriginalType; // not working at all ?
    private int ArrayLevel;
    public ArrayTypeNode(TypeNode originalType, int arrayLevel) {
        super(originalType.GetLocation(), originalType.getType());
        this.ArrayLevel = arrayLevel;
    }

    @Override
    public Type getType() {
        Type arrayType =  new Type(super.getType());
        arrayType.arrayLevel = ArrayLevel;
        return arrayType;
    }

    public int getArrayLevel() {
        return ArrayLevel;
    }
}
