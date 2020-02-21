package IR.Types;
/*
 * The instances of the Type class are immutable: once they are created,
 * they are never changed.  Also note that only one instance of a particular
 * type is ever created.  Thus seeing if two types are equal is a matter of
 * doing a trivial pointer comparison. To enforce that no two equal instances
 * are created, Type instances can only be created via static factory methods
 * in class Type and in derived classes.  Once allocated, Types are never
 * free'd.
*/


/*
 *    Inheritance Graph
 *                           {---- VoidType                                 { PointerType
 *         IRBaseType -----{---- FunctionType       {--- SingleValue --- { IntegerType
 *                         {--- FirstClassType------{
 *                                                   {---  AggregateType ---{ ArrayType
 *                                                                           { StructureType
 */

public abstract class IRBaseType {
    public enum TypeID {
        VoidTyID,        // type with no size
        HalfTyID,        // 16-bit floating point type
        FloatTyID,       // 32-bit floating point type
        DoubleTyID,      // 64-bit floating point type
        X86_FP80TyID,    // 80-bit floating point type (X87)
        FP128TyID,       // 128-bit floating point type (112-bit mantissa)
        PPC_FP128TyID,   // 128-bit floating point type (two 64-bits, PowerPC)

        LabelTyID,       // Labels -> BasicBlock
        MetadataTyID,    // Metadata
        X86_MMXTyID,     // MMX vectors (64 bits, X86 specific)
        TokenTyID,       // Tokens
        IntegerTyID,     // Arbitrary bit width integers
        FunctionTyID,    // Functions
        StructTyID,      // Structures
        ArrayTyID,       // Arrays
        PointerTyID,     // Pointers
        VectorTyID       // SIMD 'packed' format, or other vector type
    }

    ;

    protected TypeID BaseTypeName;

    protected int ByteNum;

    public abstract int getBytes();

    public TypeID getBaseTypeName() {
        return BaseTypeName;
    }
}
