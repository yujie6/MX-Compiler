package IR.Types;


/**
 * The first class types are perhaps the most important.
 * Values of these types are the only ones which can
 * be produced by instructions.
 */

public abstract class FirstClassType extends IRBaseType {
    @Override
    public int getBytes() {
        return ByteNum;
    }
}
