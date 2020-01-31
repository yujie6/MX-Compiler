package AST;

import Tools.Location;

import java.util.List;

public class MethodDecNode extends FunctionDecNode {

    private String Identifier;
    private TypeNode ReturnType;
    private List<VariableDecNode> ParaDecList;
    private BlockNode FuncBlock;
    private boolean isConstructMethod;

    public MethodDecNode(Location location,
                         BlockNode funcBlock,
                         TypeNode returnType,
                         List<VariableDecNode> paraDecList,
                         boolean isConstruct,
                         String identifier) {
        super(location, funcBlock, returnType, paraDecList, identifier);
        this.isConstructMethod = isConstruct;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
