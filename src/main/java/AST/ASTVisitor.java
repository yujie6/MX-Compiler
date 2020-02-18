package AST;

public interface ASTVisitor<T> {
    public T visit(MxProgramNode node);
    /* Declaration */
    public T visit(FunctionDecNode node);
    public T visit(VariableDecNode node);
    public T visit(ClassDecNode node);
    public T visit(MethodDecNode node);
    public T visit(TypeNode node);
    public T visit(VarDecoratorNode node);

    /* Statement */
    public T visit(IfStmtNode node);
    public T visit(BreakStmtNode node);
    public T visit(WhileStmtNode node);
    public T visit(ContinueStmtNode node);
    public T visit(ExprStmtNode node);
    public T visit(ForStmtNode node);
    public T visit(ReturnStmtNode node);
    public T visit(VarDecStmtNode node);
    public T visit(BlockNode node);
    public T visit(ConstNode node);
    public T visit(ArrayCreatorNode node);
    public T visit(ConstructCreatorNode node);

    /* Expression */
    public T visit(BinExprNode node);
    public T visit(IDExprNode node);
    public T visit(MemberExprNode node);
    public T visit(ArrayExprNode node);
    public T visit(PrefixExprNode node);
    public T visit(PostfixExprNode node);
    public T visit(ThisExprNode node);
    public T visit(CallExprNode node);

    public T visit(ParameterNode node);
}
