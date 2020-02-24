package IR;


import java.io.IOException;

public abstract class IRBaseNode {


    abstract public void accept(IRVisitor<IRBaseNode> visitor) throws IOException;
}
