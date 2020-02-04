package AST;

import Tools.Location;

public class VarDecStmtNode extends StmtNode {

    VariableDecNode variableDecNode;
    public VarDecStmtNode(Location location, VariableDecNode variableDecNode) {
        super(location);
        this.variableDecNode = variableDecNode;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
