package AST;

import Tools.Location;

public class VarDecStmtNode extends StmtNode {

    private VariableDecNode variableDecNode;

    public VarDecStmtNode(Location location, VariableDecNode variableDecNode) {
        super(location);
        this.variableDecNode = variableDecNode;
    }

    public VariableDecNode getVariableDecNode() {
        return variableDecNode;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
