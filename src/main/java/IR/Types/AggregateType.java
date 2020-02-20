package IR.Types;

public abstract class AggregateType extends FirstClassType {
    @Override
    public int getBytes() {
        return ByteNum;
    }
}
