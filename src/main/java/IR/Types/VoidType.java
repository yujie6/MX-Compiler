package IR.Types;

import IR.Value;

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

    @Override
    public Value getDefaultValue() {
        return null;
    }
}
