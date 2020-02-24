package AST;

import Tools.Location;
import java.util.List;

public class ClassDecNode extends DecNode {

    private List<VariableDecNode> VarNodeList;
    private List<MethodDecNode> MethodNodeList;
    private int AcceptStage; // used for IR builder

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
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }

    public int getAcceptStage() {
        return AcceptStage;
    }

    public void setAcceptStage(int acceptStage) {
        AcceptStage = acceptStage;
    }
}
