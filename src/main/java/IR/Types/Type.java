package IR.Types;
/// The instances of the Type class are immutable: once they are created,
/// they are never changed.  Also note that only one instance of a particular
/// type is ever created.  Thus seeing if two types are equal is a matter of
/// doing a trivial pointer comparison. To enforce that no two equal instances
/// are created, Type instances can only be created via static factory methods
/// in class Type and in derived classes.  Once allocated, Types are never
/// free'd.


public class Type {
    enum TypeID {
        // PrimitiveTypes - make sure LastPrimitiveTyID stays up to date.
        VoidTyID,    ///<  0: type with no size
        HalfTyID,        ///<  1: 16-bit floating point type

        FloatTyID,       ///<  2: 32-bit floating point type
        DoubleTyID,      ///<  3: 64-bit floating point type
        X86_FP80TyID,    ///<  4: 80-bit floating point type (X87)
        FP128TyID,       ///<  5: 128-bit floating point type (112-bit mantissa)
        PPC_FP128TyID,   ///<  6: 128-bit floating point type (two 64-bits, PowerPC)

        LabelTyID,       ///<  7: Labels -> BasicBlock
        MetadataTyID,    ///<  8: Metadata
        X86_MMXTyID,     ///<  9: MMX vectors (64 bits, X86 specific)
        TokenTyID,       ///< 10: Tokens

        // Derived types... see DerivedTypes.h file.
        // Make sure FirstDerivedTyID stays up to date!
        IntegerTyID,     ///< 11: Arbitrary bit width integers
        FunctionTyID,    ///< 12: Functions
        StructTyID,      ///< 13: Structures
        ArrayTyID,       ///< 14: Arrays
        PointerTyID,     ///< 15: Pointers
        VectorTyID       ///< 16: SIMD 'packed' format, or other vector type
    };

    private TypeID ID;
}
