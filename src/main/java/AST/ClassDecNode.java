package AST;

import Tools.Location;
import java.util.List;

public class ClassDecNode extends DecNode {

    private List<VariableDecNode> VarNodeList;
    private List<FunctionDecNode> FuncNodeList;

    public ClassDecNode(Location location,
                        List<VariableDecNode> varNodeList,
                        List<FunctionDecNode> funcNodeList) {
        super(location);
        this.VarNodeList = varNodeList;
        this.FuncNodeList = funcNodeList;
    }

    public List<FunctionDecNode> getFuncNodeList() {
        return FuncNodeList;
    }

    public List<VariableDecNode> getVarNodeList() {
        return VarNodeList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
