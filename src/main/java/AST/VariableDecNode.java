package AST;

import Tools.Location;

import java.util.List;

public class VariableDecNode extends DecNode {

    private TypeNode VarType;
    private List<VarDecoratorNode> VarDecoratorList;

    public VariableDecNode(Location location,
                           TypeNode varType,
                           List<VarDecoratorNode> varDecoratorList) {
        super(location);
        this.VarType = varType;
        this.VarDecoratorList = varDecoratorList;
    }

    public List<VarDecoratorNode> getVarDecoratorList() {
        return VarDecoratorList;
    }

    public TypeNode getVarType() {
        return VarType;
    }

    public Type getType() {
        return VarType.getType();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
