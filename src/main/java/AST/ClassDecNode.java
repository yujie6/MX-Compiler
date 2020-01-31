package AST;

import Tools.Location;
import java.util.List;

public class ClassDecNode extends DecNode {

    private List<VariableDecNode> VarNodeList;
    private List<MethodDecNode> MethodNodeList;

    public ClassDecNode(Location location,
                        String id,
                        List<VariableDecNode> varNodeList,
                        List<MethodDecNode> methodNodeList) {
        super(location);
        this.identifier = id;
        this.VarNodeList = varNodeList;
        this.MethodNodeList = methodNodeList;
    }

    public List<MethodDecNode> getMethodNodeList() {
        return MethodNodeList;
    }

    public List<VariableDecNode> getVarNodeList() {
        return VarNodeList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
