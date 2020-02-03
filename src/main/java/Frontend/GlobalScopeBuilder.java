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
            if (declaration instanceof FunctionDecNode) {
                visit((FunctionDecNode) declaration);
            } else if (declaration instanceof ClassDecNode) {
                visit((ClassDecNode) declaration);
            }
        }
        CheckMainEntry();
    }

    @Override
    public void visit(DecNode node) {
    }

    @Override
    public void visit(FunctionDecNode node) {
        FunctionEntity mx_function = new FunctionEntity(globalScope, node);
        globalScope.defineFunction(mx_function);
    }

    @Override
    public void visit(VariableDecNode node) {

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
    public void visit(StmtNode node) {

    }

    @Override
    public void visit(ExprNode node) {

    }
}
