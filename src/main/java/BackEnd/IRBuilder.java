package BackEnd;

import AST.*;
import Frontend.Scope;
import IR.*;
import IR.Module;
import IR.Types.IRBaseType;

import java.util.ArrayList;
import java.util.logging.Logger;

public class IRBuilder implements ASTVisitor {
    private ValueSymbolTable valueSymbolTable;
    private Module TopModule;
    private Scope GlobalScope;
    private Function curFunction, init;
    private BasicBlock curBasicBlock, curLoopBlock;
    private Logger logger;


    public IRBuilder(Scope globalScope, Logger logger) {
        TopModule = new Module(null);
        this.GlobalScope = globalScope;
        this.logger = logger;
        init = new Function("_entry_block", Module.VOID, new ArrayList<>());
        TopModule.defineFunction(init);
        init.initialize();
    }

    public IRBaseType ConvertTypeFromAST(Type type) {
        if (type.isBool()) {
            return Module.I1;
        } else if (type.isInt()) {
            return Module.I32;
        } else if (type.isString()) {
            return Module.STRING;
        }
        return null;
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
            if (declaration instanceof ClassDecNode) {
                for (MethodDecNode method : ((ClassDecNode) declaration).getMethodNodeList()) {
                    TopModule.defineFunction(method, declaration.getIdentifier());
                }
            } else if (declaration instanceof FunctionDecNode) {
                TopModule.defineFunction((FunctionDecNode) declaration);
            }
        }

        curBasicBlock = init.getHeadBlock();
        curFunction = init;
        for (DecNode declaration : node.getDecNodeList()) {
            if (declaration instanceof VariableDecNode) {
                declaration.accept(this);
            }
        }
        curBasicBlock = null;
        curFunction = null;

        for (DecNode declaration : node.getDecNodeList()) {
            if (declaration instanceof ClassDecNode) {
                declaration.accept(this);
            }
        }

        for (DecNode declaration : node.getDecNodeList()) {
            if (declaration instanceof VariableDecNode) {
                ((VariableDecNode) declaration).setGlobal(true);
                declaration.accept(this);
            }
        }

        return null;
    }

    @Override
    public Object visit(FunctionDecNode node) {
        return null;
    }

    @Override
    public Object visit(VariableDecNode node) {
        IRBaseType type = ConvertTypeFromAST(node.getType());
        if (node.isGlobal()) {
            for (VarDecoratorNode subnode : node.getVarDecoratorList()) {
                GlobalVariable globalVar = new GlobalVariable(type, subnode.getIdentifier(), null);
                ExprNode initExpr = subnode.getInitValue();
                if (initExpr != null) {
                    Value initValue = (Value) initExpr.accept(this);
                }
            }
        } else {

        }

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
