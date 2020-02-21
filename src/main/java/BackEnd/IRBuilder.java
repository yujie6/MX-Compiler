package BackEnd;

import AST.*;
import Frontend.Scope;
import IR.*;
import IR.Instructions.*;
import IR.Module;
import IR.Types.IRBaseType;
import IR.Types.PointerType;
import Tools.Operators;

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
        String FuncName = node.getIdentifier();
        Function function = TopModule.getFunctionMap().get(FuncName);
        curFunction = function;
        curBasicBlock = function.getHeadBlock();
        for (StmtNode stmt : node.getFuncBlock().getStmtList()) {
            stmt.accept(this);
        }
        curBasicBlock.AddInst(new BranchInst(curBasicBlock, null, function.getRetBlock(), null));
        function.AddBlock(function.getRetBlock());
        if (FuncName.equals("main")) {
            // call init at main's top block
            curFunction.getHeadBlock().MakeHeadInst(new CallInst(curBasicBlock, init, new ArrayList<>()));
        }

        curFunction = null;
        curBasicBlock = null;
        return null;
    }

    @Override
    public Object visit(VariableDecNode node) {
        IRBaseType type = ConvertTypeFromAST(node.getType());
        if (node.isGlobal()) {
            for (VarDecoratorNode subnode : node.getVarDecoratorList()) {
                GlobalVariable globalVar = new GlobalVariable(type, subnode.getIdentifier(), null);
                ExprNode initExpr = subnode.getInitValue();
                Value initValue;
                if (initExpr != null) {
                    initValue = (Value) initExpr.accept(this);
                    if (initValue.getVTy() != Value.ValueType.CONSTANT) {
                        curBasicBlock.AddInst(new StoreInst(curBasicBlock, initValue, globalVar));
                    }
                } else {
                    initValue = type.getDefaultValue();
                }
                globalVar.setInitValue(initValue);
                TopModule.defineGlobalVar(globalVar);
            }
        } else {
            // Local Variable
            for (VarDecoratorNode subnode : node.getVarDecoratorList()) {
                Register allocaAddr = new Register(subnode.getIdentifier(), new PointerType(type), null);
                BasicBlock head = curFunction.getHeadBlock();
                head.AddInst(new AllocaInst(curBasicBlock, allocaAddr, type));
                ExprNode initExpr = subnode.getInitValue();
                Value initValue;
                if (initExpr != null) {
                    initValue = (Value) initExpr.accept(this);
                    if (initValue.getVTy() != Value.ValueType.CONSTANT) {
                        curBasicBlock.AddInst(new StoreInst(curBasicBlock, initValue, allocaAddr));
                    }
                } else {
                    initValue = type.getDefaultValue();
                }
            }
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
        BasicBlock ThenBlock = new BasicBlock(curFunction, "ThenBlock");
        BasicBlock ElseBlock = (node.isHasElse()) ? new BasicBlock(curFunction, "ElseBlock") : null;
        BasicBlock MergeBlock = new BasicBlock(curFunction, "IfMergeBlock");
        Value condition = (Value) node.getConditionExpr().accept(this);
        if (node.isHasElse()) {
            curBasicBlock.AddInst(new BranchInst(curBasicBlock, condition, ThenBlock, ElseBlock));
        } else {
            curBasicBlock.AddInst(new BranchInst(curBasicBlock, condition, ThenBlock, MergeBlock));
        }

        curBasicBlock = ThenBlock;
        node.getThenStmt().accept(this);
        curBasicBlock.AddInst(new BranchInst(curBasicBlock, null, MergeBlock, null));
        curFunction.AddBlock(curBasicBlock);

        if (node.isHasElse()) {
            curBasicBlock = ElseBlock;
            node.getElseStmt().accept(this);
            curBasicBlock.AddInst(new BranchInst(curBasicBlock, null, MergeBlock, null));
            curFunction.AddBlock(curBasicBlock);
        }

        curBasicBlock = MergeBlock;
        curFunction.AddBlock(MergeBlock);

        // TODO: put block (or label) at symbol table

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
        Operators.BinaryOp bop = node.getBop();
        Value LHS = (Value) node.getLeftExpr().accept(this);
        Value RHS = (Value) node.getRightExpr().accept(this);

        switch (bop) {
            case ADD: {
                if (LHS.getType().equals(Module.I32)) {
                    Register instance = new Register("_add_int", Module.I32, null);
                    curBasicBlock.AddInst(new BinOpInst(curBasicBlock)); // TODO store to symbol table
                    return instance;
                } else if (LHS.getType().equals(Module.STRING)) {
                    // str add, need function call
                    Function function = getTopModule().getFunctionMap().get("_string_add");
                    ArrayList<Value> paras = new ArrayList<>();
                    paras.add(LHS);
                    paras.add(RHS);

                    Register instance = new Register("_string_add", Module.STRING, null);

                    curBasicBlock.AddInst(new CallInst(curBasicBlock, function, paras)); // TODO
                    return instance;
                }
                break;
            }
            case SUB:
                break;
            case MUL:
                break;
            case DIV:
                break;
            case MOD:
                break;
            case SHL:
                break;
            case SHR:
                break;
            case GREATER_EQUAL:
                break;
            case LESS_EQUAL:
                break;
            case GREATER:
                break;
            case LESS:
                break;
            case EQUAL:
                break;
            case NEQUAL:
                break;
            case BITWISE_AND:
                break;
            case BITWISE_OR:
                break;
            case BITWISE_XOR:
                break;
            case LOGIC_AND:
                break;
            case LOGIC_OR:
                break;
            case ASSIGN:
                break;
            case DEFAULT:
                break;
        }

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
