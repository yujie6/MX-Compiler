package AST;

import Tools.Location;

import java.util.List;

public class BlockNode extends ASTNode {
    List<StmtNode> StmtList;

    public BlockNode(Location location, List<StmtNode> stmtList) {
        super(location);
        this.StmtList = stmtList;
    }

    public List<StmtNode> getStmtList() {
        return StmtList;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

}
