package AST;

import Tools.Location;

import java.util.List;

public class FunctionDecNode extends DecNode {

    private String Identifier;
    private TypeNode ReturnType;
    private List<ParameterNode> ParaDecList;
    private BlockNode FuncBlock;

    public FunctionDecNode(Location location,
                           BlockNode funcBlock,
                           TypeNode returnType,
                           List<ParameterNode> paraDecList,
                           String identifier) {
        super(location);
        this.Identifier = identifier;
        this.FuncBlock = funcBlock;
        this.ReturnType = returnType;
        this.ParaDecList = paraDecList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
