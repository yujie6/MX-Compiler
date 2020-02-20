package IR;


public abstract class IRBaseNode {


    abstract public void accept(IRVisitor<IRBaseNode> visitor);
}
