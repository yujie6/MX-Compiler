package AST;

import Tools.Location;

import java.util.List;

public class FunctionDecNode extends DecNode {

    private TypeNode ReturnType;
    private List<ParameterNode> ParaDecList;
    private BlockNode FuncBlock;

    public FunctionDecNode(Location location,
                           BlockNode funcBlock,
                           TypeNode returnType,
                           List<ParameterNode> paraDecList,
                           String identifier) {
        super(location);
        this.identifier = identifier;
        this.FuncBlock = funcBlock;
        this.ReturnType = returnType;
        this.ParaDecList = paraDecList;
    }

    public List<ParameterNode> getParaDecList() {
        return ParaDecList;
    }

    public TypeNode getReturnType() {
        return ReturnType;
    }

    public BlockNode getFuncBlock() {
        return FuncBlock;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
