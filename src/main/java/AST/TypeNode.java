package AST;

import Tools.Location;

public class TypeNode extends ASTNode {
    private Type type;
    public TypeNode(Location location, Type type) {
        super(location);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }


}
