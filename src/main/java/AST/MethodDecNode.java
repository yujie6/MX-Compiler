package AST;

import Tools.Location;

import java.util.List;

public class MethodDecNode extends FunctionDecNode {

    private boolean isConstructMethod;

    public MethodDecNode(Location location,
                         BlockNode funcBlock,
                         TypeNode returnType,
                         List<ParameterNode> paraDecList,
                         boolean isConstruct,
                         String identifier) {
        super(location, funcBlock, returnType, paraDecList, identifier);
        this.isConstructMethod = isConstruct;
    }

    public boolean isConstructMethod() {
        return isConstructMethod;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
