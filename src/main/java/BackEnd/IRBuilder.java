package BackEnd;

import AST.*;
import Frontend.Scope;
import IR.*;
import IR.Instructions.*;
import IR.Module;
import IR.Types.IRBaseType;
import MxEntity.FunctionEntity;
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

    public static IRBaseType ConvertTypeFromAST(Type type) {
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
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, function.getRetBlock(), null));
        function.AddBlockAtTail(function.getRetBlock());
        if (FuncName.equals("main")) {
            // call init at main's top block
            CallInst call = new CallInst(curBasicBlock, init, new ArrayList<>());
            curFunction.getHeadBlock().AddInstAtTop(call);
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
                        curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, initValue, globalVar));
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

                BasicBlock head = curFunction.getHeadBlock();
                AllocaInst AllocaAddr = new AllocaInst(curBasicBlock, type);
                head.AddInstAtTail(AllocaAddr);
                ExprNode initExpr = subnode.getInitValue();
                Value initValue;
                if (initExpr != null) {
                    initValue = (Value) initExpr.accept(this);
                    if (initValue.getVTy() != Value.ValueType.CONSTANT) {
                        curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, initValue, AllocaAddr));
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
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, condition, ThenBlock, ElseBlock));
        } else {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, condition, ThenBlock, MergeBlock));
        }

        curBasicBlock = ThenBlock;
        node.getThenStmt().accept(this);
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, MergeBlock, null));
        curFunction.AddBlockAtTail(curBasicBlock);

        if (node.isHasElse()) {
            curBasicBlock = ElseBlock;
            node.getElseStmt().accept(this);
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, MergeBlock, null));
            curFunction.AddBlockAtTail(curBasicBlock);
        }

        curBasicBlock = MergeBlock;
        curFunction.AddBlockAtTail(MergeBlock);

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
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.add, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance); // TODO store to symbol table
                    return instance;
                } else if (LHS.getType().equals(Module.STRING)) {
                    // str add, need function call
                    Function function = getTopModule().getFunctionMap().get("_string_add");
                    ArrayList<Value> paras = new ArrayList<>();
                    paras.add(LHS);
                    paras.add(RHS);
                    CallInst instance = new CallInst(curBasicBlock, function, paras);
                    curBasicBlock.AddInstAtTail(instance); // TODO
                    return instance;
                }
                break;
            }
            case SUB: {
                // only integer sub
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use sub on non integer type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.sub, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case MUL: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use mul on non integer type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.mul, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case DIV: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use div on non integer type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.div, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case MOD: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use mod on non integer type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.srem, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case SHL: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use left shift on non integer type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.shl, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case SHR: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use right shift on non integer type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.shr, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case LESS_EQUAL:
            case GREATER:
            case LESS:
            case EQUAL:
            case NEQUAL:
            case GREATER_EQUAL: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use ge on non integer type.");
                } else {
                    CmpInst instance = new CmpInst(curBasicBlock, Module.I1, bop, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }

            case BITWISE_AND: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use bitwise and on non integer type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.and, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case BITWISE_OR: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use bitwise or on non integer type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.or, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case BITWISE_XOR: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use bitwise xor on non integer type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.xor, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case LOGIC_AND: {
                if (!LHS.getType().equals(Module.I1)) {
                    logger.warning("Use logic and on non bool type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I1, Instruction.InstType.and, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case LOGIC_OR: {
                if (!LHS.getType().equals(Module.I1)) {
                    logger.warning("Use logic or on non bool type.");
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I1, Instruction.InstType.or, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
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
        FunctionEntity mx_func = node.getFunction();
        if (mx_func.isMethod()) {

        } else {
            Function CalledFunc = TopModule.getFunctionMap().get(mx_func.getIdentifier());
            ArrayList<Value> args = new ArrayList<>();
            for (ExprNode expr: node.getParameters() ) {
                Value arg = (Value) expr.accept(this);
                args.add(arg);
            }
            CallInst instance = new CallInst(curBasicBlock, CalledFunc, args);
            curBasicBlock.AddInstAtTail(instance);
            return instance;
        }
        return null;
    }

    @Override
    public Object visit(ParameterNode node) {
        return null;
    }
}
