package IR.Types;

import IR.Constants.IntConst;
import IR.Value;

import java.util.ArrayList;

/**
 *  The array type is a very simple derived type that arranges elements sequentially
 *  in memory. The array type requires a size (number of elements) and an underlying data type.
 */
public class ArrayType extends AggregateType {
    private ArrayList<Value> sizeList;
    private IRBaseType ArrayBaseType;

    public ArrayType(ArrayList<Value> size, IRBaseType baseType) {
        this.BaseTypeName = TypeID.ArrayTyID;
        this.ArrayBaseType = baseType;
        this.sizeList = size;
        this.ByteNum = ArrayBaseType.getBytes();
        // compute bytNum ...
    }

    public ArrayList<Value> getSizeList() {
        return sizeList;
    }

    public IRBaseType getArrayBaseType() {
        return ArrayBaseType;
    }

    @Override
    public Value getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        for (Value size : sizeList) {
            ans.append("[").append( ((IntConst) size).getValue() );
            ans.append(" x ");
        }
        ans.append(getArrayBaseType().toString());
        for (Value size : sizeList) {
            ans.append("]");
        }
        return ans.toString();
    }

    @Override
    public IRBaseType getElementType(ArrayList<Value> offsets) {
        if (offsets.size() == sizeList.size()) {
            return getArrayBaseType();
        } else if (offsets.size() < sizeList.size()) {

        } else {

        }
        return null;
    }
}
