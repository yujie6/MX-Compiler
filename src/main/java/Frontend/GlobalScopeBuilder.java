package Frontend;

import AST.*;
import MxEntity.*;
import Tools.MXError;

public class GlobalScopeBuilder implements ASTVisitor {
    private Scope globalScope;

    public GlobalScopeBuilder() {
        globalScope = new Scope();
    }

    private void CheckMainEntry() {

        if (!globalScope.hasFunction("main")) {
            throw new MXError("Program entry \"main\" doesn't exist");
        } else {
            FunctionEntity mainFunc = globalScope.GetFunction("main");
            if (mainFunc.getParaListSize() != 0) {
                throw new MXError("Program entry \"main\" has more than one argument");
            } else if (mainFunc.getReturnType().getBaseType() != BaseType.DTYPE_INT) {
                throw new MXError("Progran entry \"main\" should has int return type");
            }
        }
     }

    public Scope getGlobalScope() {
        return globalScope;
    }

    private void PreProcess() {
        ClassEntity mx_string = new ClassEntity("string", globalScope);
        globalScope.defineClass(mx_string);
        /*
        TODO : ADD Build-in Function
         */
    }

    @Override
    public void visit(MxProgramNode node) {
        PreProcess();
        for (DecNode declaration : node.getDecNodeList()) {
            declaration.accept(this);
        }
        CheckMainEntry();
    }

    @Override
    public void visit(FunctionDecNode node) {
        FunctionEntity mx_function = new FunctionEntity(globalScope, node, false, null);
        globalScope.defineFunction(mx_function);
    }

    @Override
    public void visit(VariableDecNode node) {
        Type DecType = node.getType();
        for (VarDecoratorNode var : node.getVarDecoratorList()) {
            VariableEntity mx_var = new VariableEntity(globalScope, var, DecType);
            globalScope.defineVariable(mx_var);
        }
    }

    @Override
    public void visit(ClassDecNode node) {
        ClassEntity mx_class = new ClassEntity(globalScope, node);
        globalScope.defineClass(mx_class);
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

    @Override
    public void visit(MethodDecNode node) {
        
    }
}
