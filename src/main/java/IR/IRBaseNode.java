package IR;


public abstract class IRBaseNode {
    abstract public Object accept(IRVisitor<Object> visitor);
}
