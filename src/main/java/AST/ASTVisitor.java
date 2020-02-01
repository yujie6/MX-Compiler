package AST;

public interface ASTVisitor {
    public void visit(MxProgramNode node);
    public void visit(DecNode node);
    public void visit(FunctionDecNode node);
    public void visit(VariableDecNode node);
    public void visit(ClassDecNode node);


    public void visit(TypeNode node);

    public void visit(BlockNode node);

    public void visit(VarDecoratorNode node);

    public void visit(ConstNode node);
}
