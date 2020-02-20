package IR;

public class Function extends Value
{
    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) {
        visitor.visit(this);
    }
}
