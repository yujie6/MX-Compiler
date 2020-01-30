package AST;

import Tools.Location;
import org.antlr.v4.codegen.model.decl.Decl;

public class DeclarationNode extends ASTNode {

    public DeclarationNode(Location location) {
        super(location);

    }


    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

}
