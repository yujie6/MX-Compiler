package AST;

import java.util.List;
import Tools.Location;


public class MxProgramNode extends ASTNode {
    private List<DecNode> DecNodeList;

    public MxProgramNode(Location location, List<DecNode> decNodeList) {
        super(location);
        this.DecNodeList = decNodeList;
    }

    public List<DecNode> getDecNodeList() {
        return DecNodeList;
    }

    @Override
    public void accept(ASTVisitor visitor)
    {
        visitor.visit(this);
    }
}
