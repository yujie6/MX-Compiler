package AST;

import Tools.Location;
import org.antlr.v4.codegen.model.decl.Decl;

public abstract class DecNode extends ASTNode {

    protected String identifier;

    public DecNode(Location location) {
        super(location);
    }

    public String getIdentifier() {
        return identifier;
    }

    public abstract void accept(ASTVisitor visitor);

}
