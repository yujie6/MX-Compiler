package BackEnd;

import AST.*;
import Frontend.Scope;
import IR.*;
import IR.Constants.*;
import IR.Instructions.*;
import IR.Module;
import IR.Types.ArrayType;
import IR.Types.IRBaseType;
import IR.Types.PointerType;
import IR.Types.StructureType;
import MxEntity.FunctionEntity;
import Tools.Operators;

import javax.print.DocFlavor;
import java.awt.image.TileObserver;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Logger;

public class IRBuilder implements ASTVisitor {
    private ValueSymbolTable valueSymbolTable;
    private Module TopModule;
    private Scope GlobalScope;
    private Function curFunction, init;
    private BasicBlock curBasicBlock, curLoopBlock;
    public Logger logger;

    private Stack<BasicBlock> CondStackForBreak;
    private Stack<BasicBlock> LoopStackForContinue;

    public IRBuilder(Scope globalScope, Logger logger) {
        TopModule = new Module(null, logger);
        this.GlobalScope = globalScope;
        this.logger = logger;
        CondStackForBreak = new Stack<>();
        LoopStackForContinue = new Stack<>();
        init = new Function("_entry_block", Module.VOID, new ArrayList<>());
        TopModule.defineFunction(init);
        init.initialize();
        logger.info("IRBuilder construction complete.");
    }

    public static IRBaseType ConvertTypeFromAST(Type type) {
        if (type.isBool()) {
            return Module.I1;
        } else if (type.isInt()) {
            return Module.I32;
        } else if (type.isString()) {
            return Module.STRING;
        } else if (type.isClass()) {
            return new StructureType(type.getName(), null);
        } else if (type.isArray()) {
            return new ArrayType(0, ConvertTypeFromAST(
                    new Type(type.getBaseType(), type.getArrayLevel(), type.getName())
            ));
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
                ((ClassDecNode) declaration).setAcceptStage(0);
                declaration.accept(this);
            }
        }
        // Stage 0: put class in ClassMap
        TopModule.RefreshClassMapping();


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
                ((VariableDecNode) declaration).setGlobal(true);
                declaration.accept(this);
            }
        }
        curBasicBlock = null;
        curFunction = null;

        for (DecNode declaration : node.getDecNodeList()) {
            if (declaration instanceof ClassDecNode) {
                ((ClassDecNode) declaration).setAcceptStage(1);
                declaration.accept(this);
            }
        }

        for (DecNode declaration : node.getDecNodeList()) {
            if (declaration instanceof FunctionDecNode) {
                declaration.accept(this);
            }
        }

        logger.fine("IR build complete with no errors!");

        return TopModule;
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
        // curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, function.getRetBlock(), null));
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
                    initValue = (Instruction) initExpr.accept(this);
                    if (initValue.getVTy() != Value.ValueType.CONSTANT) {
                        curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, initValue, globalVar));
                        // init global var at init's first block
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
                curFunction.getVarSymTab().put(subnode.getIdentifier(), AllocaAddr);
                head.AddInstAtTail(AllocaAddr);
                ExprNode initExpr = subnode.getInitValue();
                Value initValue;
                if (initExpr != null) {
                    initValue = (Instruction) initExpr.accept(this);
                    //if (initValue.getVTy() != Value.ValueType.CONSTANT) {
                    curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, initValue, AllocaAddr));
                    //}
                }
//                else {
//                    initValue = type.getDefaultValue();
//                    curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, initValue, AllocaAddr));
//                }
            }
        }

        return null;
    }

    @Override
    public Object visit(ClassDecNode node) {
        if (node.getAcceptStage() == 0) {
            ArrayList<IRBaseType> MemberList = new ArrayList<>();
            for (VariableDecNode subnode : node.getVarNodeList()) {
                IRBaseType memberType = ConvertTypeFromAST(subnode.getType());
                assert memberType != null;
//                if (memberType.getBaseTypeName() == IRBaseType.TypeID.StructTyID) {
//                    memberType = new PointerType(memberType);
//                }
                for (VarDecoratorNode var : subnode.getVarDecoratorList()) {
                    MemberList.add(memberType);
                }
            }
            StructureType curClass = new StructureType(node.getIdentifier(), MemberList);
            TopModule.defineClass(node.getIdentifier(), curClass);
        } else if (node.getAcceptStage() == 1) {

            for (VariableDecNode subnode : node.getVarNodeList()) {
                subnode.accept(this);
            }

            for (MethodDecNode method : node.getMethodNodeList()) {
                method.accept(this);
            }
        }
        return null;
    }

    @Override
    public Object visit(MethodDecNode node) {
        String methodName = node.getIdentifier();
        assert TopModule.getFunctionMap().containsKey(methodName);
        Function method = TopModule.getFunctionMap().get(methodName);
        curFunction = method;
        curBasicBlock = method.getHeadBlock();
        node.getFuncBlock().accept(this);
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, method.getRetBlock(), null));
        method.AddBlockAtTail(method.getRetBlock());

        curFunction = null;
        curBasicBlock = null;
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
        if (!condition.getType().getBaseTypeName().equals(IRBaseType.TypeID.IntegerTyID)) {
            logger.severe("Condition is not boolean type");
        }
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
        curBasicBlock.AddInstAtTail(new BranchInst(
                curBasicBlock, null, CondStackForBreak.peek(), null
        ));
        return null;
    }

    @Override
    public Object visit(WhileStmtNode node) {
        BasicBlock CondBlock = new BasicBlock(curFunction, "WhileCondition");
        BasicBlock LoopBody = new BasicBlock(curFunction, "WhileLoopBody");
        BasicBlock MergeBlock = new BasicBlock(curFunction, "WhileMergeBlock");
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, CondBlock, null));
        curBasicBlock = CondBlock;
        Instruction cond = (Instruction) node.getCondition().accept(this);
        // TODO: check cast's correctness
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, cond, LoopBody, MergeBlock));
        LoopStackForContinue.push(LoopBody);
        CondStackForBreak.push(CondBlock);
        curBasicBlock = LoopBody;
        node.getLoopStmt().accept(this);
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, CondBlock, null));
        curBasicBlock = MergeBlock;

        LoopStackForContinue.pop();
        CondStackForBreak.pop();
        curFunction.AddBlockAtTail(CondBlock);
        curFunction.AddBlockAtTail(LoopBody);
        curFunction.AddBlockAtTail(MergeBlock);

        TopModule.defineLabel(CondBlock);
        TopModule.defineLabel(LoopBody);
        TopModule.defineLabel(MergeBlock);

        return null;
    }

    @Override
    public Object visit(ContinueStmtNode node) {
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null,
                LoopStackForContinue.peek(), null));
        return null;
    }

    @Override
    public Object visit(ExprStmtNode node) {
        node.getExpr().accept(this);
        return null;
    }

    @Override
    public Object visit(ForStmtNode node) {
        BasicBlock LoopBody = new BasicBlock(curFunction, "ForLoopBody");
        BasicBlock UpdateBlock = new BasicBlock(curFunction, "ForUpdate");
        BasicBlock CondBlock = new BasicBlock(curFunction, "ForCondBlock");
        BasicBlock MergeBlock = new BasicBlock(curFunction, "ForMergeBlock");

        if (node.getInitExpr() != null) {
            Instruction initInst = (Instruction) node.getInitExpr().accept(this);
        }
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, CondBlock, null));

        curBasicBlock = CondBlock;

        Instruction condInst = (Instruction) node.getCondExpr().accept(this);
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, condInst, LoopBody, MergeBlock));
        LoopStackForContinue.push(LoopBody);
        CondStackForBreak.push(CondBlock);

        curBasicBlock = LoopBody;
        node.getLoopBlcok().accept(this);
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, CondBlock, null));

        LoopStackForContinue.pop();
        CondStackForBreak.pop();

        curBasicBlock = UpdateBlock;
        Instruction updateInst = (Instruction) node.getUpdateExpr().accept(this);
        // could only be assign expr
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, CondBlock, null));

        curBasicBlock = MergeBlock;

        curFunction.AddBlockAtTail(LoopBody);
        curFunction.AddBlockAtTail(UpdateBlock);
        curFunction.AddBlockAtTail(CondBlock);
        curFunction.AddBlockAtTail(MergeBlock);
        TopModule.defineLabel(LoopBody);
        TopModule.defineLabel(UpdateBlock);
        TopModule.defineLabel(CondBlock);
        TopModule.defineLabel(MergeBlock);


        return null;
    }

    @Override
    public Object visit(ReturnStmtNode node) {
        if (node.getReturnedExpr() != null) {
            Instruction exprInst = (Instruction) node.getReturnedExpr().accept(this);
            curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, exprInst, curFunction.getRetValue()));
        }
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null,
                curFunction.getRetBlock(), null));
        return null;
    }

    @Override
    public Object visit(VarDecStmtNode node) {
        node.getVariableDecNode().accept(this);
        return null;
    }

    @Override
    public Object visit(BlockNode node) {
        for (StmtNode stmt : node.getStmtList()) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Object visit(ConstNode node) {
        IRBaseType constType = ConvertTypeFromAST(node.getType());
        Constant constant = null;
        if (constType.equals(Module.STRING)) {
            constant = new StringConst(((StringConstNode) node).getValue());
        } else if (constType.equals(Module.I32)) {
            constant = new IntConst(((IntConstNode) node).getValue());
        } else if (constType.equals(Module.I1)) {
            constant = new BoolConst(((BoolConstNode) node).getValue());
        } else if (constType.getBaseTypeName().equals(IRBaseType.TypeID.ArrayTyID)) {
            // constant = new ArrayConst();
        }
        if (constant == null) {
            logger.severe("Constant processing error;");
        }
        return constant;
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
        isLeftValue = bop.equals(Operators.BinaryOp.ASSIGN);
        Value LHS = (Value) node.getLeftExpr().accept(this);
        isLeftValue = false;
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
            case ASSIGN: {
                // RHS may be const or inst
                // LHS must be address (alloca)
                curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, RHS, LHS));
                break;
            }
            case DEFAULT:
                break;
        }

        return null;
    }

    private boolean isLeftValue;

    @Override
    public Object visit(IDExprNode node) {
        // depends on it's left or right value
        Value VarAddr;
        if (curFunction.getVarSymTab().contains(node.getIdentifier())) {
            VarAddr = curFunction.getVarSymTab().get(node.getIdentifier());
        } else {
            VarAddr = TopModule.getGlobalVarMap().get(node.getIdentifier());
        }
        if (!(VarAddr instanceof AllocaInst)) {
            logger.warning("Variable definition error, not storing an address.");
        }

        if (!isLeftValue) {
            Instruction loadResult = new LoadInst(curBasicBlock, ((AllocaInst) VarAddr).getBaseType(), VarAddr);
            curBasicBlock.AddInstAtTail(loadResult);
            return loadResult;
        } else {
            return VarAddr;
        }
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
            for (ExprNode expr : node.getParameters()) {
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
