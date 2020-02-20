package IR;

import AST.*;
import Frontend.Scope;

public class IRBuilder implements ASTVisitor {
    private ValueSymbolTable valueSymbolTable;
    private Module TopModule;
    private Scope GlobalScope;
    private Function curFunction;
    private BasicBlock curBasicBlock, curLoopBlock;


    public IRBuilder(Scope globalScope) {
        TopModule = new Module(null);
        this.GlobalScope = globalScope;

    }

    public Module getTopModule() {
        return TopModule;
    }

    public BasicBlock getCurBasicBlock() {
        return curBasicBlock;
    }

    public BasicBlock getCurLoopBlock() {
        return curLoopBlock;
    }

    public Function getCurFunction() {
        return curFunction;
    }

    @Override
    public Object visit(MxProgramNode node) {
        for (DecNode declaration : node.getDecNodeList()) {

        }
        return null;
    }

    @Override
    public Object visit(FunctionDecNode node) {
        return null;
    }

    @Override
    public Object visit(VariableDecNode node) {
        return null;
    }

    @Override
    public Object visit(ClassDecNode node) {
        return null;
    }

    @Override
    public Object visit(MethodDecNode node) {
        return null;
    }

    @Override
    public Object visit(TypeNode node) {
        return null;
    }

    @Override
    public Object visit(VarDecoratorNode node) {
        return null;
    }

    @Override
    public Object visit(IfStmtNode node) {
        return null;
    }

    @Override
    public Object visit(BreakStmtNode node) {
        return null;
    }

    @Override
    public Object visit(WhileStmtNode node) {
        return null;
    }

    @Override
    public Object visit(ContinueStmtNode node) {
        return null;
    }

    @Override
    public Object visit(ExprStmtNode node) {
        return null;
    }

    @Override
    public Object visit(ForStmtNode node) {
        return null;
    }

    @Override
    public Object visit(ReturnStmtNode node) {
        return null;
    }

    @Override
    public Object visit(VarDecStmtNode node) {
        return null;
    }

    @Override
    public Object visit(BlockNode node) {
        return null;
    }

    @Override
    public Object visit(ConstNode node) {
        return null;
    }

    @Override
    public Object visit(ArrayCreatorNode node) {
        return null;
    }

    @Override
    public Object visit(ConstructCreatorNode node) {
        return null;
    }

    @Override
    public Object visit(BinExprNode node) {
        return null;
    }

    @Override
    public Object visit(IDExprNode node) {
        return null;
    }

    @Override
    public Object visit(MemberExprNode node) {
        return null;
    }

    @Override
    public Object visit(ArrayExprNode node) {
        return null;
    }

    @Override
    public Object visit(PrefixExprNode node) {
        return null;
    }

    @Override
    public Object visit(PostfixExprNode node) {
        return null;
    }

    @Override
    public Object visit(ThisExprNode node) {
        return null;
    }

    @Override
    public Object visit(CallExprNode node) {
        return null;
    }

    @Override
    public Object visit(ParameterNode node) {
        return null;
    }
}
