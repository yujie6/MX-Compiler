package Frontend;

import AST.*;

import java.util.HashMap;

/**
 * @apiNote Used to do type referencing
 */
public class SymbolTable implements ASTVisitor {

    // public HashMap<String, Value>
    private Type LocalRetType;
    private Scope GlobalScope, LocalScope;

    public SymbolTable(Scope gs) {
        this.GlobalScope = gs;
    }

    @Override
    public void visit(MxProgramNode node) {

    }

    @Override
    public void visit(FunctionDecNode node) {

    }

    @Override
    public void visit(VariableDecNode node) {

    }

    @Override
    public void visit(ClassDecNode node) {

    }

    @Override
    public void visit(MethodDecNode node) {

    }

    @Override
    public void visit(TypeNode node) {

    }

    @Override
    public void visit(BlockNode node) {

    }

    @Override
    public void visit(VarDecoratorNode node) {

    }

    @Override
    public void visit(ConstNode node) {

    }

    @Override
    public void visit(CreatorNode node) {

    }

    @Override
    public void visit(BinExprNode node) {

    }

    @Override
    public void visit(IDExprNode node) {

    }

    @Override
    public void visit(MemberExprNode node) {

    }

    @Override
    public void visit(ArrayExprNode node) {

    }

    @Override
    public void visit(PrefixExprNode node) {

    }

    @Override
    public void visit(PostfixExprNode node) {

    }

    @Override
    public void visit(ThisExprNode node) {

    }

    @Override
    public void visit(CallExprNode node) {

    }

    @Override
    public void visit(IfStmtNode node) {

    }

    @Override
    public void visit(BreakStmtNode node) {

    }

    @Override
    public void visit(WhileStmtNode node) {

    }

    @Override
    public void visit(ContinueStmtNode node) {

    }

    @Override
    public void visit(ExprStmtNode node) {

    }

    @Override
    public void visit(ForStmtNode node) {

    }

    @Override
    public void visit(ReturnStmtNode node) {

    }

    @Override
    public void visit(VarDecStmtNode node) {

    }

    @Override
    public void visit(ParameterNode node) {

    }
}
