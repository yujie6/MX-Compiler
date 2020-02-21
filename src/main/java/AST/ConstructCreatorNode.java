package AST;

import Tools.Location;

public class ConstructCreatorNode extends CreatorNode{
    boolean UseConstructMethod;
    public ConstructCreatorNode(Location location, TypeNode typeNode, boolean useConstructMethod) {
        super(location, typeNode);
        this.UseConstructMethod = useConstructMethod;
        this.ExprType = typeNode.getType();
    }

    public boolean isUseConstructMethod() {
        return UseConstructMethod;
    }


    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
