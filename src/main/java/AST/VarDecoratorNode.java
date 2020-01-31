package AST;

import Tools.Location;

public class VarDecoratorNode extends ASTNode {
    private String Identifier;
    private ExprNode InitValue;

    public VarDecoratorNode(Location location, String identifier, ExprNode initValue) {
        super(location);
        this.Identifier = identifier;
        this.InitValue = initValue;
    }

    public ExprNode getInitValue() {
        return InitValue;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
