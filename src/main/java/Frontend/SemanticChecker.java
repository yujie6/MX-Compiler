package Frontend;

import AST.*;
import MxEntity.ClassEntity;
import MxEntity.Entity;
import MxEntity.FunctionEntity;
import MxEntity.VariableEntity;
import Tools.MXError;
import Tools.Operators;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

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

    private Scope getFatherScope() {
        return EnteredScope.lastElement();
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
            VariableEntity mx_para = new VariableEntity(LocalScope, para);
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
        Type DecType = node.getType();
        // check if the type is defined
        if (DecType.getBaseType() == BaseType.STYPE_CLASS) {
            if (!LocalScope.hasClass(DecType.getName())) {
                throw new MXError("Invalid class: " + DecType.getName(), node.GetLocation());
            }
        }
        for (VarDecoratorNode var : node.getVarDecoratorList()) {
            VariableEntity mx_var = new VariableEntity(LocalScope, var, DecType);
            LocalScope.defineVariable(mx_var);
        }
    }

    @Override
    public void visit(ClassDecNode node) {
        ClassEntity classEntity = GlobalScope.GetClass(node.getIdentifier());
        EnterScope(classEntity);
        for (VariableDecNode MemberDecNode : node.getVarNodeList()) {
            visit(MemberDecNode);
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
    public void visit(BinExprNode node) {
        if (node.getBop().equals(Operators.BinaryOp.ASSIGN)) {
            // Assign statement: check left value (Array/member/id)
            ExprNode LHS = node.getLeftExpr();
            if (!(LHS instanceof MemberExprNode || LHS instanceof IDExprNode ||
                    LHS instanceof ArrayExprNode)) {
                throw new MXError("Assign statement has wrong left value.", node.GetLocation());
            }
            node.getLeftExpr().accept(this);
            node.getRightExpr().accept(this);
        }
    }

    @Override
    public void visit(IDExprNode node) {
        // what if this is an method's name
        if (! (LocalScope.hasVariable(node.getIdentifier()) ||
                LocalScope.hasClass(node.getIdentifier()) ||
                LocalScope.hasFunction(node.getIdentifier()) )) {
            throw new MXError(String.format("Variable or class or function \"%s\" is not defined", node.getIdentifier()),
                    node.GetLocation());
        }
    }

    @Override
    public void visit(MemberExprNode node) {
        // after type checking
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ArrayExprNode node) {
        node.getArrayId().accept(this);
        node.getOffset().accept(this);
    }

    @Override
    public void visit(PrefixExprNode node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(PostfixExprNode node) {
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ThisExprNode node) {
        if (!LocalScope.inClass) {
            throw new MXError("Using \"this\" out of a class domain.", node.GetLocation());
        }
    }

    @Override
    public void visit(CallExprNode node) {
        node.getObj().accept(this);
        // if (node.getParameters().size() != )
        for (ExprNode para : node.getParameters()) {
           para.accept(this);
        }
        // node.getParameters().forEach();
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
        if (node.getCondition() == null) {
            throw new MXError("While statement has no condition expr.", node.GetLocation());
        } else {
            node.getCondition().accept(this);
        }
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
        // damn it ++a; (a); (++a); a + a; are all valid statement!!
        // only <array,member,id> can be count as left value
        // a = 123 for example, what  about a = b = 1 ?
        node.getExpr().accept(this);
    }

    @Override
    public void visit(ForStmtNode node) {
        LocalScope.LoopLevel++;
        if (node.getCondExpr() != null) {
            node.getCondExpr().accept(this);
        }
        if (node.getInitExpr() != null) {
            node.getInitExpr().accept(this);
        }
        if (node.getUpdateExpr() != null) {
            node.getUpdateExpr().accept(this);
        }
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
        visit(node.getVariableDecNode());
    }

    @Override
    public void visit(ParameterNode node) {

    }
}
