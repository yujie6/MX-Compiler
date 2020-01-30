package AST;

import Tools.Location;

import java.util.List;
import Tools.Location;


public class MxProgramNode extends ASTNode {
    private List<DeclarationNode> DeclChildren;

    public MxProgramNode(Location location, List<DeclarationNode> declChildren) {
        super(location);
        this.DeclChildren = declChildren;
    }

    @Override
    public void accept(ASTVisitor visitor)
    {
        visitor.visit(this);
    }
}
