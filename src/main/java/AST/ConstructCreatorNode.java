package AST;

import Tools.Location;

public class ConstructCreatorNode extends CreatorNode{
    boolean UseConstructMethod;
    public ConstructCreatorNode(Location location, TypeNode typeNode, boolean useConstructMethod) {
        super(location, typeNode);
        this.UseConstructMethod = useConstructMethod;
    }
}
