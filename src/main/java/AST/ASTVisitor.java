package AST;

public interface ASTVisitor {
    public void visit(MxProgramNode node);
    public void visit(DeclarationNode node);

}
