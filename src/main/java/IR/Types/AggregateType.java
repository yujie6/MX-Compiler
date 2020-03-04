package IR.Types;

import IR.Value;

import java.util.ArrayList;

public abstract class AggregateType extends FirstClassType {
    @Override
    public int getBytes() {
        return ByteNum;
    }

    public abstract IRBaseType getElementType(ArrayList<Value> offsets);
}
