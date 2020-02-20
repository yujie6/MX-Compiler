package IR.Types;

/**
 *  The pointer type is used to specify memory locations. Pointers are
 *  commonly used to reference objects in memory.
 *  Pointer types may have an optional address space attribute defining
 *  the numbered address space where the pointed-to object resides. (default zero)
 *
 *    Examples:
 *    [4 x i32]*	A pointer to array of four i32 values.
 *    i32 (i32*) *	A pointer to a function that takes an i32*, returning an i32.
 *    i32 addrspace(5)*	A pointer to an i32 value that resides in address space #5.
 */

public class PointerType extends SingleValueType {
    private IRBaseType PtrBaseType;

    public PointerType (IRBaseType baseType) {
        this.BaseTypeName = TypeID.PointerTyID;
        this.PtrBaseType = baseType;
    }

    public IRBaseType getBaseType() {
        return PtrBaseType;
    }

    @Override
    public int getBytes() {
        return 4;
    }
}
