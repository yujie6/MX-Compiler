package IR.Types;

public class IntegerType extends SingleValueType {

    private BitWidth bitWidth;

    public enum BitWidth {
        i1, i8, i16, i32
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
        }
    }

    public BitWidth getBitWidth() {
        return bitWidth;
    }
}
