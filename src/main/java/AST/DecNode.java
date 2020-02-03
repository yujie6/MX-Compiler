package AST;

import Tools.Location;
import org.antlr.v4.codegen.model.decl.Decl;

public class DecNode extends ASTNode {

    protected String identifier;

    public DecNode(Location location) {
        super(location);
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

}
