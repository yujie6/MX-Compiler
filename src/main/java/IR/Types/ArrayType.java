package IR.Types;

import IR.Value;

import java.util.ArrayList;

/**
 *  The array type is a very simple derived type that arranges elements sequentially
 *  in memory. The array type requires a size (number of elements) and an underlying data type.
 */
public class ArrayType extends AggregateType {
    private int size;
    private IRBaseType ArrayBaseType;

    public ArrayType(int size, IRBaseType baseType) {
        this.BaseTypeName = TypeID.ArrayTyID;
        this.ArrayBaseType = baseType;
        this.size = size;
        this.ByteNum = size * ArrayBaseType.getBytes();
    }

    public int getSize() {
        return size;
    }

    public IRBaseType getArrayBaseType() {
        return ArrayBaseType;
    }

    @Override
    public Value getDefaultValue() {
        return null;
    }
}
