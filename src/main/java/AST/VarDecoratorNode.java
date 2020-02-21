package AST;

import Tools.Location;

public class VarDecoratorNode extends ASTNode {
    private String Identifier;
    private ExprNode InitValue;
    private Type InitType;

    public VarDecoratorNode(Location location, String identifier, ExprNode initValue) {
        super(location);
        this.Identifier = identifier;
        this.InitValue = initValue;
    }

    public ExprNode getInitValue() {
        return InitValue;
    }

    public String getIdentifier() {
        return Identifier;
    }

    public void setInitType(Type initType) {
        InitType = initType;
    }

    public Type getInitType() {
        return InitType;
    }

    @Override
    public Object accept(ASTVisitor visitor) {
        visitor.visit(this);
        return null;
    }
}
