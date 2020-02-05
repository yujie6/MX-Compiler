package Frontend;

import AST.*;
import MxEntity.ClassEntity;
import MxEntity.Entity;
import MxEntity.FunctionEntity;
import MxEntity.VariableEntity;
import Tools.MXError;
import Tools.Operators;

import java.util.Stack;

public class SemanticChecker implements ASTVisitor {

    /**
     * @implSpec Declaration, block and loop have their
     * own scope, and the LocalScope will change when visiting
     * these nodes
     * @apiNote
     * 1. Check if a var/func/class is defined
     * 2. Check if they are defined more than once
     */
    private Scope GlobalScope, LocalScope;
    private Stack<Scope> EnteredScope;
    public SemanticChecker(Scope gs) {
        EnteredScope = new Stack<>();
        this.GlobalScope = gs;
    }

    private void EnterScope(Entity mx_entity) {
        EnteredScope.push(LocalScope);
        if (mx_entity != null) LocalScope = mx_entity.getScope();
    }

    private void ExitScope() {
        LocalScope = EnteredScope.lastElement();
        EnteredScope.pop();
    }

    @Override
    public void visit(MxProgramNode node) {
        LocalScope = GlobalScope;
        for (DecNode declaration : node.getDecNodeList()) {
            declaration.accept(this);
        }
    }

    @Override
    public void visit(FunctionDecNode node) {
        FunctionEntity functionEntity = LocalScope.GetFunction(node.getIdentifier());
        EnterScope(functionEntity);
        for (ParameterNode para : node.getParaDecList()) {
            VariableEntity mx_para = new VariableEntity(para);
            LocalScope.defineVariable(mx_para);
        }
        // TODO special cases
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
        ClassEntity classEntity = GlobalScope.GetClass(node.getIdentifier());
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
        visit((FunctionDecNode) node);
    }

    @Override
    public void visit(TypeNode node) {

    }

    @Override
    public void visit(BlockNode node) {
        EnterScope(null);
        for (StmtNode statement : node.getStmtList()) {
            statement.accept(this);
        }
        ExitScope();
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
        node.getThenStmt().accept(this);
        if (node.isHasElse()) {
            node.getElseStmt().accept(this);
        }
    }

    @Override
    public void visit(BreakStmtNode node) {
        // must be in a loop
        if (LocalScope.LoopLevel <= 0) {
            throw new MXError("Continue statement not in a loop");
        }
    }

    @Override
    public void visit(WhileStmtNode node) {
        LocalScope.LoopLevel++;
        node.getLoopStmt().accept(this);
        LocalScope.LoopLevel--;
    }

    @Override
    public void visit(ContinueStmtNode node) {
        // must be in a loop
        if (LocalScope.LoopLevel <= 0) {
            throw new MXError("Continue statement not in a loop");
        }
    }

    @Override
    public void visit(ExprStmtNode node) {
        // TODO check left value
        // damn it ++a; (a); (++a); a + a; are all valid statement!!
        // only <array,member,id> can be count as left value
        // a = 123 for example, what  about a = b = 1 ?
    }

    @Override
    public void visit(ForStmtNode node) {
        LocalScope.LoopLevel++;
        node.getLoopBlcok().accept(this);
        LocalScope.LoopLevel--;
    }

    @Override
    public void visit(ReturnStmtNode node) {
        if (LocalScope.inFunction = false) {
            throw new MXError("Return statement not in a function");
        }
    }

    @Override
    public void visit(VarDecStmtNode node) {

    }

    @Override
    public void visit(ExprNode node) {

    }

    @Override
    public void visit(ParameterNode node) {

    }
}
