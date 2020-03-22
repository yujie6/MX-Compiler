package IR.Types;

import IR.Constants.IntConst;
import IR.Value;

public class IntegerType extends SingleValueType {

    private BitWidth bitWidth;

    @Override
    public Value getDefaultValue() {
        return new IntConst( 0);
    }

    @Override
    public String toString() {
        return bitWidth.toString();
    }

    public enum BitWidth {
        i1, i8, i16, i32, i64
    }

    public IntegerType(BitWidth bitWidth) {
        this.BaseTypeName = TypeID.IntegerTyID;
        this.bitWidth = bitWidth;
        switch (bitWidth) {
            case i1:
            case i8: {
                ByteNum = 1;
                break;
            }
            case i16: {
                ByteNum = 2;
                break;
            }
            case i32: {
                ByteNum = 4;
                break;
            }
            case i64: {
                ByteNum = 8;
                break;
            }
        }
    }

    public BitWidth getBitWidth() {
        return bitWidth;
    }
}
