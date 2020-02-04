package Frontend;

import AST.*;
import MxEntity.ClassEntity;
import MxEntity.Entity;
import MxEntity.FunctionEntity;
import MxEntity.VariableEntity;
import Tools.Location;

public class SemanticChecker implements ASTVisitor {

    /**
     * @implSpec Declaration, block and loop have their
     * own scope, and the LocalScope will change when visiting
     * these nodes
     * @apiNote
     * 1. Check if a var/func/class is defined
     * 2. Check if they are defined more than once
     */
    private Scope GlogalScope, LocalScope;
    public SemanticChecker(Scope gs) {
        this.GlogalScope = gs;
    }

    private void EnterScope(Entity mx_entity) {
        LocalScope = mx_entity.getScope();
    }

    private void ExitScope() {
        LocalScope = GlogalScope;
    }

    @Override
    public void visit(MxProgramNode node) {
        LocalScope = GlogalScope;
        for (DecNode declaration : node.getDecNodeList()) {
            if (declaration instanceof FunctionDecNode) {
                visit((FunctionDecNode) declaration);
            } else if (declaration instanceof ClassDecNode) {
                visit((ClassDecNode) declaration);
            } else if (declaration instanceof VariableDecNode) {
                visit((VariableDecNode) declaration);
            }
        }
    }

    @Override
    public void visit(DecNode node) {

    }

    @Override
    public void visit(FunctionDecNode node) {
        FunctionEntity functionEntity = GlogalScope.GetFunction(node.getIdentifier());
        EnterScope(functionEntity);
        for (ParameterNode para : node.getParaDecList()) {
            VariableEntity mx_para = new VariableEntity(para);
            LocalScope.defineVariable(mx_para);
        }

        for (StmtNode statement : node.getFuncBlock().getStmtList()) {
            statement.accept(this);
        }

        ExitScope();
    }

    @Override
    public void visit(VariableDecNode node) {

    }

    @Override
    public void visit(ClassDecNode node) {
        ClassEntity classEntity = GlogalScope.GetClass(node.getIdentifier());
        EnterScope(classEntity);
        for (VariableDecNode LocalVar : node.getVarNodeList()) {
            VariableEntity LocalMxVar = new VariableEntity(LocalVar);
            LocalScope.defineVariable(LocalMxVar);
        }

        for (MethodDecNode method : node.getMethodNodeList()) {
            FunctionEntity LocalMethod = new FunctionEntity(LocalScope, method, true, node.getIdentifier());
            LocalScope.defineFunction(LocalMethod);
        }

        for (MethodDecNode method : node.getMethodNodeList()) {
            visit(method);
        }
        ExitScope();
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
    public void visit(ExprNode node) {

    }
}
