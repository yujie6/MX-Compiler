package IR.Types;

public class VoidType extends IRBaseType {

    public VoidType() {
        this.BaseTypeName = TypeID.VoidTyID;
    }

    @Override
    public int getBytes() {
        return 0;
    }
}
