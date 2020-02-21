package IR.Types;

/**
 *  The void type does not represent any value and has no size.
 */
public class VoidType extends IRBaseType {

    public VoidType() {
        this.BaseTypeName = TypeID.VoidTyID;
    }

    @Override
    public int getBytes() {
        return 0;
    }
}
