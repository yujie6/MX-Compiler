package AST;

public interface ASTVisitor {
    public void visit(MxProgramNode node);
    /* Declaration */
    public void visit(DecNode node);
    public void visit(FunctionDecNode node);
    public void visit(VariableDecNode node);
    public void visit(ClassDecNode node);
    public void visit(MethodDecNode node);

    public void visit(TypeNode node);
    public void visit(BlockNode node);
    public void visit(VarDecoratorNode node);

    /* Statement */
    // public void visit(StmtNode node);
    public void visit(IfStmtNode node);
    public void visit(BreakStmtNode node);
    public void visit(WhileStmtNode node);
    public void visit(ContinueStmtNode node);
    public void visit(ExprStmtNode node);
    public void visit(ForStmtNode node);
    public void visit(ReturnStmtNode node);
    public void visit(VarDecStmtNode node);

    public void visit(ConstNode node);
    public void visit(CreatorNode node);

    /* Expression */
    public void visit(ExprNode node);

}
