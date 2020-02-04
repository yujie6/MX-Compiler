package Frontend;

import AST.*;
import MxEntity.*;
import Tools.MXError;

public class GlobalScopeBuilder implements ASTVisitor {
    Scope globalScope;

    public GlobalScopeBuilder() {
        globalScope = new Scope();
    }

    public void CheckMainEntry() {
        FunctionEntity mainFunc = globalScope.GetFunction("main");
        if (mainFunc == null) {
            throw new MXError("Program entry \"main\" doesn't exist");
        } else if (mainFunc.getParaListSize() != 0) {
            throw new MXError("Program entry \"main\" has more than one argument");
        } else if (mainFunc.getReturnType().getBaseType() != BaseType.DTYPE_INT) {
            throw new MXError("Progran entry \"main\" should has int return type");
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
        VariableEntity mx_var = new VariableEntity(node);
        globalScope.defineVariable(mx_var);
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

    @Override
    public void visit(ParameterNode node) {

    }

    @Override
    public void visit(MethodDecNode node) {
        
    }
}
