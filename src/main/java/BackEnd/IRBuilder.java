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
import MxEntity.ClassEntity;
import MxEntity.FunctionEntity;
import Tools.Location;
import Tools.MXLogger;
import Tools.Operators;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

public class IRBuilder implements ASTVisitor {
    private ValueSymbolTable valueSymbolTable;
    private static Module TopModule;
    private Scope GlobalScope;
    private Function curFunction, init;
    private BasicBlock curBasicBlock, curLoopBlock;
    static public MXLogger logger;

    private Stack<BasicBlock> CondStackForBreak;
    private Stack<BasicBlock> LoopStackForContinue;
    private Stack<ValueSymbolTable> EnteredTable;
    ValueSymbolTable LocalSymTab;

    private void EnterScope(Function function) {
        EnteredTable.push(LocalSymTab.clone());
        if (function != null) LocalSymTab = function.getVarSymTab();
    }

    private void ExitScope() {
        LocalSymTab = EnteredTable.peek();
        EnteredTable.pop();
    }

    public IRBuilder(Scope globalScope, MXLogger logger) {
        TopModule = new Module(null, logger);
        this.GlobalScope = globalScope;
        IRBuilder.logger = logger;
        CondStackForBreak = new Stack<>();
        LoopStackForContinue = new Stack<>();
        EnteredTable = new Stack<>();
        init = new Function("_entry_block", Module.VOID, new ArrayList<>(), false);
        TopModule.defineFunction(init);
        init.initialize();
        logger.info("IRBuild ready to start.");
    }

    public static IRBaseType ConvertTypeFromAST(Type type) {
        if (type.isArray()) {
            Type point_to = new Type(type);
            point_to.setArrayLevel(type.getArrayLevel() - 1);
            return new PointerType(ConvertTypeFromAST(point_to));
        } else if (type.isBool()) {
            return Module.I1; // shall be i8
        } else if (type.isInt()) {
            return Module.I32;
        } else if (type.isString()) {
            return Module.STRING;
        } else if (type.isVoid()) {
            return Module.VOID;
        } else if (type.isNull()) {

        } else if (type.isClass()) {
            return TopModule.getClassMap().get(type.getName());
        }
        logger.severe("Bad type transfer!");
        System.exit(1);
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
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, curFunction.getRetBlock(), null));
        curFunction.AddBlockAtTail(curFunction.getRetBlock());
        curBasicBlock = null;
        curFunction = null;

        LocalSymTab = new ValueSymbolTable(TopModule.getGlobalVarMap());

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
        EnterScope(function);
        curFunction = function;
        curBasicBlock = function.getHeadBlock();
        if (FuncName.equals("main")) {
            StoreInst storeZeroToRet = new StoreInst(curBasicBlock, new IntConst(0), function.getRetValue());
            curBasicBlock.AddInstAtTail(storeZeroToRet);
        }
        for (StmtNode stmt : node.getFuncBlock().getStmtList()) {
            stmt.accept(this);
        }
        if (!curBasicBlock.endWithBranch()) {
            // deal with cases with no return stmt
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, function.getRetBlock(), null));
        }

        function.AddBlockAtTail(function.getRetBlock());
        if (FuncName.equals("main")) {
            // call init at main's top block
            CallInst call = new CallInst(curBasicBlock, init, new ArrayList<>());
            curFunction.getHeadBlock().AddInstAtTop(call);
        }
        ExitScope();
        logger.fine("IR build for function " + node.getIdentifier() + " done.");
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
                    initValue = (Value) initExpr.accept(this); // could be print or new expr
                    globalVar.setInitValue(initValue);
                    if (initValue instanceof AllocaInst) {
                        globalVar.setType(initValue.getType());
                        globalVar.setOriginalType(((AllocaInst) initValue).getBaseType());
                    }
                    if (initValue.getVTy() != Value.ValueType.CONSTANT) {
                        curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, initValue, globalVar));
                        // init global var at init's first block
                    }
                } else {
                    initValue = type.getDefaultValue();
                    globalVar.setInitValue(initValue);
                }
                globalVar.setInitValue(initValue);
                TopModule.defineGlobalVar(globalVar);
                logger.fine("IR build for '" + subnode.getIdentifier() + "' global variable done.");
            }
        } else {
            // Local Variable
            for (VarDecoratorNode subnode : node.getVarDecoratorList()) {

                BasicBlock head = curFunction.getHeadBlock();
                AllocaInst AllocaAddr = new AllocaInst(curBasicBlock, type);
                LocalSymTab.put(subnode.getIdentifier(), AllocaAddr);
                head.AddInstAtTop(AllocaAddr);
                ExprNode initExpr = subnode.getInitValue();
                Value initValue;
                if (initExpr != null) {
                    initValue = (Value) initExpr.accept(this);
                    curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, initValue, AllocaAddr));
                }
                logger.fine("IR build for '" + subnode.getIdentifier() + "' variable declaration ir done.");
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

//            for (VariableDecNode subnode : node.getVarNodeList()) {
//                subnode.accept(this);
//            }

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
            logger.severe("Condition is not boolean type", node.GetLocation());
        }
        if (node.isHasElse()) {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, condition, ThenBlock, ElseBlock));
        } else {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, condition, ThenBlock, MergeBlock));
        }

        curBasicBlock = ThenBlock;
        node.getThenStmt().accept(this); // curBasicBlock may change here
        if (!curBasicBlock.endWithBranch()) {
            // else is the return inst
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, MergeBlock, null));
        }
        curFunction.AddBlockAtTail(ThenBlock);

        if (node.isHasElse()) {
            curBasicBlock = ElseBlock;
            node.getElseStmt().accept(this);
            if (!curBasicBlock.endWithBranch()) {
                curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, MergeBlock, null));
            }
            curFunction.AddBlockAtTail(ElseBlock);
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
        return node.getExpr().accept(this);
    }

    @Override
    public Object visit(ForStmtNode node) {
        BasicBlock LoopBody = new BasicBlock(curFunction, "ForLoopBody");
        BasicBlock UpdateBlock = new BasicBlock(curFunction, "ForUpdate");
        BasicBlock CondBlock = new BasicBlock(curFunction, "ForCondBlock");
        BasicBlock MergeBlock = new BasicBlock(curFunction, "ForMergeBlock");

        if (node.getInitExpr() != null) {
            Instruction initInst = (Instruction) node.getInitExpr().accept(this);
            curBasicBlock.AddInstAtTail(initInst);
        }
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, CondBlock, null));

        curBasicBlock = CondBlock;

        if (node.getCondExpr() != null) {
            Instruction condInst = (Instruction) node.getCondExpr().accept(this);
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, condInst, LoopBody, MergeBlock));
            LoopStackForContinue.push(LoopBody);
            CondStackForBreak.push(CondBlock);
        } else {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, LoopBody, null));
            LoopStackForContinue.push(LoopBody);
            CondStackForBreak.push(CondBlock);
        }

        curBasicBlock = LoopBody;
        node.getLoopBlcok().accept(this);
        if (!curBasicBlock.endWithBranch()) {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, UpdateBlock, null));
        }
        LoopStackForContinue.pop();
        CondStackForBreak.pop();

        curBasicBlock = UpdateBlock;
        node.getUpdateExpr().accept(this);
        // could only be assign expr
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, CondBlock, null));

        curBasicBlock = MergeBlock;

        curFunction.AddBlockAtTail(LoopBody);
        curFunction.AddBlockAtTail(UpdateBlock);
        curFunction.AddBlockAtTail(CondBlock);
        curFunction.AddBlockAtTail(MergeBlock);
//        TopModule.defineLabel(LoopBody);
//        TopModule.defineLabel(UpdateBlock);
//        TopModule.defineLabel(CondBlock);
//        TopModule.defineLabel(MergeBlock);
        return null;
    }

    @Override
    public Object visit(ReturnStmtNode node) {
        if (node.getReturnedExpr() != null) {
            Value RetValue;
            if (node.getReturnedExpr() instanceof ConstNode) {
                RetValue = (Constant) node.getReturnedExpr().accept(this);
            } else {
                boolean old_left = isLeftValue;
                isLeftValue = false;
                RetValue = (Instruction) node.getReturnedExpr().accept(this);
                isLeftValue = old_left;
            }
            curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, RetValue, curFunction.getRetValue()));
        }
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, curFunction.getRetBlock(), null));
        return null;
    }

    @Override
    public Object visit(VarDecStmtNode node) {
        node.getVariableDecNode().accept(this);
        return null;
    }

    @Override
    public Object visit(BlockNode node) {
        EnterScope(null);
        for (StmtNode stmt : node.getStmtList()) {
            stmt.accept(this);
        }
        ExitScope();
        return null;
    }

    @Override
    public Object visit(ConstNode node) {
        IRBaseType constType = ConvertTypeFromAST(node.getType());
        Constant constant = null;
        if (constType.equals(Module.STRING)) {
            // string const -> global var
            String str_const = ((StringConstNode) node).getValue();
            constant = new StringConst(str_const);
            GlobalVariable globalString;

            if (TopModule.getStringConstMap().containsKey(str_const)) {
                globalString = TopModule.getStringConstMap().get(str_const);
            } else {
                globalString = new GlobalVariable(constType, null, constant);
                TopModule.getStringConstMap().put(str_const, globalString);
                TopModule.getGlobalVarMap().put(globalString.getIdentifier(), globalString);
            }

            ArrayList<Value> offsetList = new ArrayList<>();
            offsetList.add(new IntConst(0));
            offsetList.add(new IntConst(0));
            GetPtrInst strPtr = new GetPtrInst(curBasicBlock, globalString, offsetList, Module.I8);
            curBasicBlock.AddInstAtTail(strPtr);
            return strPtr;

        } else if (constType.equals(Module.I32)) {
            constant = new IntConst(((IntConstNode) node).getValue());
        } else if (constType.equals(Module.I1)) {
            constant = new BoolConst(((BoolConstNode) node).getValue());
        } else if (constType.getBaseTypeName().equals(IRBaseType.TypeID.ArrayTyID)) {
            // constant = new ArrayConst();
        }
        if (constant == null) {
            logger.severe("Constant processing error;", node.GetLocation());
        }
        return constant;
    }

    @Override
    public Object visit(ArrayCreatorNode node) {
        // first only consider the simplest situation, that is new int[2][3]
        int arrayLevel = node.getArrayLevel();
        int sizeLen = node.getExprList().size();

        ArrayList<Value> sizeList = new ArrayList<>();
        boolean old_left = isLeftValue;
        isLeftValue = false;
        for (ExprNode expr : node.getExprList()) {
            sizeList.add((Value) expr.accept(this));
        }
        isLeftValue = old_left;

        Type baseType = new Type(node.getExprType());
        baseType.setArrayLevel(arrayLevel - sizeLen);
        IRBaseType arrayBaseType = ConvertTypeFromAST(baseType);

        Value array_addr = getNewArray(arrayBaseType, arrayLevel, sizeList, node.GetLocation());

        return array_addr;
    }

    private Value getNewArray(IRBaseType arrayBaseType, int arrayLevel, ArrayList<Value> arraySizeList, Location location) {
        int arrayUnitSize = (arrayLevel > 1) ? 8 : 4;
        Value arraySize = arraySizeList.get(0);
        BinOpInst malloc_size = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.mul, arraySize, new IntConst(arrayUnitSize));
        BinOpInst total_size = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.add, malloc_size, new IntConst(8));
        SextInst malloc_size_i64 = new SextInst(curBasicBlock, Module.I32, malloc_size, Module.I64);

        ArrayList<Value> paras = new ArrayList<>();
        paras.add(malloc_size_i64);
        Function _malloc = TopModule.getFunctionMap().get("_malloc_and_init");
        CallInst malloc_addr = new CallInst(curBasicBlock, _malloc, paras);
        curBasicBlock.AddInstAtTail(malloc_size);
        curBasicBlock.AddInstAtTail(total_size);
        curBasicBlock.AddInstAtTail(malloc_size_i64);
        curBasicBlock.AddInstAtTail(malloc_addr);

        if (!arraySize.getType().equals(Module.I32)) {
            logger.warning("Array size must be int32", location);
            logger.severe("Fatal error, exit");
            System.exit(1);
        }
        BitCastInst ArraySizeAddr = new BitCastInst(curBasicBlock, malloc_addr, new PointerType(Module.I64));
        SextInst arraySizeInt64 = new SextInst(curBasicBlock, Module.I32, arraySize, Module.I64);
        StoreInst stArraySize = new StoreInst(curBasicBlock, arraySizeInt64, ArraySizeAddr);
        BitCastInst array_addr = new BitCastInst(curBasicBlock, malloc_addr, new PointerType(arrayBaseType));
        ArrayList<Value> offsets = new ArrayList<>();
        offsets.add(new IntConst(8 / arrayUnitSize));
        GetPtrInst arrayBaseAddr = new GetPtrInst(curBasicBlock, array_addr, offsets, arrayBaseType);

        curBasicBlock.AddInstAtTail(ArraySizeAddr);
        curBasicBlock.AddInstAtTail(arraySizeInt64);
        curBasicBlock.AddInstAtTail(stArraySize);
        curBasicBlock.AddInstAtTail(array_addr);
        curBasicBlock.AddInstAtTail(arrayBaseAddr);

        if (arraySizeList.size() == 1) {
            return arrayBaseAddr;
        } else if (!(arrayBaseType instanceof PointerType)) {
            logger.warning("multilevel array type error.", location);
        }

        // multiLevel array, init recursively
        BasicBlock condBlock = new BasicBlock(curFunction, "ArrayInitCond");
        BasicBlock loopBlock = new BasicBlock(curFunction, "ArrayInitLoop");
        BasicBlock updateBlock = new BasicBlock(curFunction, "ArrayInitUpdate");
        BasicBlock mergeBlock = new BasicBlock(curFunction, "ArrayInitMerge");
        IRBaseType subArrayType = new PointerType(arrayBaseType);
        AllocaInst subArrayAddr = new AllocaInst(curFunction.getHeadBlock(), subArrayType);
        curFunction.getHeadBlock().AddInstAtTop(subArrayAddr);
        StoreInst stBase = new StoreInst(curBasicBlock, arrayBaseAddr, subArrayAddr);
        curBasicBlock.AddInstAtTail(stBase);
        offsets = new ArrayList<>();
        offsets.add(arraySize);
        GetPtrInst arrayTail = new GetPtrInst(curBasicBlock, arrayBaseAddr, offsets, arrayBaseType);
        curBasicBlock.AddInstAtTail(arrayTail);

        curBasicBlock = condBlock;
        LoadInst subArray = new LoadInst(curBasicBlock, subArrayType, subArrayAddr);
        CmpInst subArrayReachTail = new CmpInst(curBasicBlock, subArrayType, Operators.BinaryOp.LESS, subArray,
                arrayTail);
        BranchInst jump = new BranchInst(curBasicBlock, subArrayReachTail, loopBlock, mergeBlock);
        curBasicBlock.AddInstAtTail(subArray);
        curBasicBlock.AddInstAtTail(subArrayReachTail);
        curBasicBlock.AddInstAtTail(jump);

        curBasicBlock = loopBlock;
        IRBaseType subArrayBaseType = ((PointerType) arrayBaseType).getBaseType();
        arraySizeList.remove(0);
        Value _subArray = getNewArray(subArrayBaseType, arrayLevel - 1, arraySizeList, location);
        StoreInst stSubArray = new StoreInst(curBasicBlock, _subArray, subArray);
        curBasicBlock.AddInstAtTail(stSubArray);
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, updateBlock, null));

        curBasicBlock = updateBlock;
        offsets = new ArrayList<>();
        offsets.add(new IntConst(1));
        GetPtrInst nextSubArray = new GetPtrInst(curBasicBlock, arrayBaseAddr, offsets, arrayBaseType);
        StoreInst stNextSubArray = new StoreInst(curBasicBlock, nextSubArray, subArrayAddr);
        curBasicBlock.AddInstAtTail(nextSubArray);
        curBasicBlock.AddInstAtTail(stNextSubArray);
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, condBlock, null));
        curBasicBlock = mergeBlock;



        curFunction.AddBlockAtTail(condBlock);
        curFunction.AddBlockAtTail(loopBlock);
        curFunction.AddBlockAtTail(updateBlock);
        curFunction.AddBlockAtTail(mergeBlock);

        return arrayBaseAddr;
    }

    @Override
    public Object visit(ConstructCreatorNode node) {
        if (!TopModule.getClassMap().containsKey(node.getExprType().getName())) {
            logger.severe("Fatal error, new expr's class could not be found.", node.GetLocation());
            System.exit(1);
        }
        String name = node.getExprType().getName();
        StructureType classType = TopModule.getClassMap().get(name);
        if (TopModule.getFunctionMap().containsKey(name + '.' + name)) {
            Function constructor = TopModule.getFunctionMap().get(name + '.' + name);
            CallInst funcCall = new CallInst(curBasicBlock, constructor, new ArrayList<>());
            curBasicBlock.AddInstAtTail(funcCall);
            return funcCall;
        } else {
            // malloc some space, and bitcast to (class*)
            Function malloc = TopModule.getFunctionMap().get("malloc");
            ArrayList<Value> paras = new ArrayList<>();
            paras.add(new IntConst(classType.getBytes()));
            CallInst mallocCall = new CallInst(curBasicBlock, malloc, paras);
            BitCastInst instancePointer = new BitCastInst(curBasicBlock, mallocCall,
                    new PointerType(classType));


            curBasicBlock.AddInstAtTail(mallocCall);
            curBasicBlock.AddInstAtTail(instancePointer);
            return instancePointer;
        }
    }

    @Override
    public Object visit(BinExprNode node) {
        Operators.BinaryOp bop = node.getBop();

        boolean old_left = isLeftValue;
        isLeftValue = bop.equals(Operators.BinaryOp.ASSIGN);
        Value LHS = (Value) node.getLeftExpr().accept(this);
        if (LHS instanceof GetPtrInst) {
            if (!isLeftValue) {
                LoadInst lhs_instance = new LoadInst(curBasicBlock, ((GetPtrInst) LHS).getElementType(), LHS);
                if (lhs_instance.getType() == null) {
                    logger.severe("LHS is null type", node.GetLocation());
                }
                curBasicBlock.AddInstAtTail(lhs_instance);
                LHS = lhs_instance;
            }
        }
        isLeftValue = false;
        Value RHS = (Value) node.getRightExpr().accept(this);
        if (RHS instanceof GetPtrInst ) {
            if (!(node.getRightExpr() instanceof ConstNode) && !(node.getRightExpr() instanceof ArrayCreatorNode)) {
                LoadInst rhs_instance = new LoadInst(curBasicBlock, ((GetPtrInst) RHS).getElementType(), RHS);
                curBasicBlock.AddInstAtTail(rhs_instance);
                RHS = rhs_instance;
            }
        }
        isLeftValue = old_left;

        if (LHS == null || RHS == null) {
            logger.severe("Fatal error, binOp encounter null", node.GetLocation());
            logger.severe("exit now");
            System.exit(1);
        }
        switch (bop) {
            case ADD: {
                if (LHS.getType().equals(Module.I32)) {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.add, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance); // TODO store to symbol table
                    return instance;
                } else if (LHS.getType().equals(Module.STRING)) {
                    // str add, need function call
                    Function function = getTopModule().getFunctionMap().get("__string_concatenate");
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
                    logger.severe("Use sub on non integer type.", node.GetLocation());
                    System.exit(1);
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.sub, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case MUL: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use mul on non integer type.", node.GetLocation());
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.mul, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case DIV: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use div on non integer type.", node.GetLocation());
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.sdiv, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case MOD: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use mod on non integer type.", node.GetLocation());
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.srem, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case SHL: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use left shift on non integer type.", node.GetLocation());
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.shl, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case SHR: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use right shift on non integer type.", node.GetLocation());
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
                if (LHS.getType() == null) {
                    logger.severe("LHS type null", node.GetLocation());
                    logger.severe("Fatal error.");
                    System.exit(1);
                }
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use ge or equal on non integer type.", node.GetLocation());
                } else {
                    CmpInst instance = new CmpInst(curBasicBlock, Module.I1, bop, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }

            case BITWISE_AND: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use bitwise and on non integer type.", node.GetLocation());
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.and, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case BITWISE_OR: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use bitwise or on non integer type.", node.GetLocation());
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.or, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case BITWISE_XOR: {
                if (!LHS.getType().equals(Module.I32)) {
                    logger.warning("Use bitwise xor on non integer type.", node.GetLocation());
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.xor, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case LOGIC_AND: {
                if (!LHS.getType().equals(Module.I1)) {
                    logger.warning("Use logic and on non bool type.", node.GetLocation());
                } else {
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I1, Instruction.InstType.and, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
                    return instance;
                }
                break;
            }
            case LOGIC_OR: {
                if (!LHS.getType().equals(Module.I1)) {
                    logger.warning("Use logic or on non bool type.", node.GetLocation());
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
        if (LocalSymTab.contains(node.getIdentifier())) {
            VarAddr = LocalSymTab.get(node.getIdentifier());

        } else {
            if (!TopModule.getGlobalVarMap().containsKey(node.getIdentifier())) {
                logger.severe("Variable " + node.getIdentifier() + " not defined in IRBuilder.",
                        node.GetLocation());
                System.exit(1);
            }
            // Global variable
            VarAddr = TopModule.getGlobalVarMap().get(node.getIdentifier());
            if (isLeftValue) {
                return VarAddr;
            } else {
                GlobalVariable gvar = ((GlobalVariable) VarAddr);
                LoadInst globalVar = new LoadInst(curBasicBlock, gvar.getOriginalType(), VarAddr);
                curBasicBlock.AddInstAtTail(globalVar);
                return globalVar;
            }
        }


        if (!(VarAddr instanceof AllocaInst)) {
            logger.warning("Variable definition error, not storing an address.", node.GetLocation());
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
        // TODO return getelementptr inst
        Type classType = node.getExpr().getExprType();
        if (!classType.isClass() && !classType.isString() && !classType.isArray()) {
            logger.severe("Member is not used on a class object.", node.GetLocation());
            System.exit(1);
        }
        if (classType.isString() || classType.isArray()) {
            return node.getExpr().accept(this);
        }
        // what about method here ??? return getExpr directly
        StructureType classStructure = TopModule.getClassMap().get(classType.getName());
        ClassEntity classEntity = GlobalScope.GetClass(classType.getName());
        classEntity.getMember(node.getMember());
        int offset = classEntity.getMemberOffset(node.getMember());
        GetPtrInst memberPtr;
        Value ExprPointer = (Value) node.getExpr().accept(this);
        if (!(ExprPointer instanceof GetPtrInst)) {
            ArrayList<Value> offsets = new ArrayList<>();
            offsets.add(new IntConst(offset));
            IRBaseType eleType = classStructure.getElementType(offset);
            memberPtr = new GetPtrInst(curBasicBlock, ExprPointer, offsets, eleType);
        } else {
            memberPtr = new GetPtrInst((GetPtrInst) ExprPointer,
                    new IntConst(offset),
                    classStructure.getElementType(offset));
        }
        curBasicBlock.AddInstAtTail(memberPtr);
        return memberPtr;
    }

    @Override
    public Object visit(ArrayExprNode node) {

        // TODO Wrong element Type !!!, first get array at 0, then get off_t element!!
        // TODO even after that we need to load from the ptr
        boolean old_left = isLeftValue;
        isLeftValue = false; // to get pure pointer
        Value array = (Value) node.getArrayId().accept(this);
        Value offset = (Value) node.getOffset().accept(this);
        isLeftValue = old_left;
        if (ConvertTypeFromAST(node.getExprType()) == null) {
            logger.warning("Bad convert", node.GetLocation());
        }

        ArrayList<Value> offsets = new ArrayList<>();
        // offsets.add(new IntConst(0));
        offsets.add(offset);
        GetPtrInst addr = new GetPtrInst(curBasicBlock, array, offsets, ConvertTypeFromAST(node.getExprType()));
        curBasicBlock.AddInstAtTail(addr);
        if (!isLeftValue) {
            LoadInst inst = new LoadInst(curBasicBlock, addr.getElementType(), addr);
            curBasicBlock.AddInstAtTail(inst);
            return inst;
        }
        return addr;
    }

    @Override
    public Object visit(PrefixExprNode node) {
        Value expr = (Value) node.getExpr().accept(this);
        switch (node.getPrefixOp()) {

            case INC: {
                // require value
                Instruction res = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.add, expr,
                        new IntConst(1));

                // require addr
                boolean old_left = isLeftValue;
                isLeftValue = true;
                Value addr = (Value) node.getExpr().accept(this);
                isLeftValue = old_left;

                Instruction st = new StoreInst(curBasicBlock, res, addr);
                curBasicBlock.AddInstAtTail(res);
                curBasicBlock.AddInstAtTail(st);
                // return the new value
                return expr;
            }
            case DEC: {
                // require value
                Instruction res = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.sub, expr,
                        new IntConst(1));

                // require addr
                boolean old_left = isLeftValue;
                isLeftValue = true;
                Value addr = (Value) node.getExpr().accept(this);
                isLeftValue = old_left;
                Instruction st = new StoreInst(curBasicBlock, res, addr);
                curBasicBlock.AddInstAtTail(res);
                curBasicBlock.AddInstAtTail(st);
                return expr;
            }
            case POS: {
                return expr;
            }
            case NEG: {
                if (expr instanceof IntConst) {
                    int value = ((IntConst) expr).ConstValue;
                    return new IntConst(-value);
                } else {
                    BinOpInst negExpr = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.sub,
                            new IntConst(0), expr);
                    curBasicBlock.AddInstAtTail(negExpr);
                    return negExpr;
                }
            }
            case LOGIC_NOT: {
                if (expr instanceof BoolConst) {
                    boolean val = (boolean) ((BoolConst) expr).getValue();
                    return new BoolConst(!val);
                } else {
                    BinOpInst notExpr = new BinOpInst(curBasicBlock, Module.I1, Instruction.InstType.xor, expr,
                            new BoolConst(true));
                    curBasicBlock.AddInstAtTail(notExpr);
                    return notExpr;
                }
            }
            case BITWISE_NOT: {
                BinOpInst notExpr = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.xor, expr,
                        new IntConst(-1));
                curBasicBlock.AddInstAtTail(notExpr);
                return notExpr;
            }
            case DEFAULT:
                break;
        }
        return null;
    }

    @Override
    public Object visit(PostfixExprNode node) {
        isLeftValue = false;
        Value expr = (Value) node.getExpr().accept(this);
        switch (node.getPostfixOp()) {
            case INC: {
                // require value
                Instruction res = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.add, expr,
                        new IntConst(1));

                // require addr
                boolean old_left = isLeftValue;
                isLeftValue = true;
                expr = (Value) node.getExpr().accept(this);
                isLeftValue = false;
                Value store_value = (Value) node.getExpr().accept(this);
                isLeftValue = old_left;

                Instruction st = new StoreInst(curBasicBlock, res, expr);
                curBasicBlock.AddInstAtTail(res);
                curBasicBlock.AddInstAtTail(st);

                return store_value;
            }
            case DEC: {
                // require value
                Instruction res = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.sub, expr,
                        new IntConst(1));

                // require addr
                boolean old_left = isLeftValue;
                isLeftValue = true;
                expr = (Value) node.getExpr().accept(this);
                isLeftValue = false;
                Value storeValue = (Value) node.getExpr().accept(this);
                ;
                isLeftValue = old_left;
                Instruction st = new StoreInst(curBasicBlock, res, expr);
                curBasicBlock.AddInstAtTail(res);
                curBasicBlock.AddInstAtTail(st);

                return storeValue;
            }
            case DEFAULT:
                break;
        }
        return null;
    }

    @Override
    public Object visit(ThisExprNode node) {
        return null;
    }

    @Override
    public Object visit(CallExprNode node) {
        // copy the argument using memcpy
        FunctionEntity mx_func = node.getFunction();
        if (mx_func.isMethod()) {
            Function CalledFunc = null;
            if (mx_func.getClassName().equals("string")) {
                CalledFunc = TopModule.getFunctionMap().get(
                        "__string_" + mx_func.getIdentifier()
                );
                ArrayList<Value> args = new ArrayList<>();

                boolean old_left = isLeftValue;
                isLeftValue = false;
                Value str_instance = (Value) node.getObj().accept(this);
                isLeftValue = old_left;
                args.add(str_instance);

                for (ExprNode expr : node.getParameters()) {
                    Value arg = (Value) expr.accept(this);
                    args.add(arg);
                }
                CallInst instance = new CallInst(curBasicBlock, CalledFunc, args);
                curBasicBlock.AddInstAtTail(instance);
                logger.fine("IR build for '" + CalledFunc.getIdentifier() + "' method call done.", node.GetLocation());
                return instance;

            } else if (mx_func.getClassName().equals("__Array")) {
                CalledFunc = TopModule.getFunctionMap().get(
                        "__array_" + mx_func.getIdentifier()
                );
                ArrayList<Value> args = new ArrayList<>();

                boolean old_left = isLeftValue;
                isLeftValue = false;
                Value array_instance = (Value) node.getObj().accept(this);
                isLeftValue = old_left;

                BitCastInst addr = new BitCastInst(curBasicBlock, array_instance, Module.ADDR);
                curBasicBlock.AddInstAtTail(addr);
                args.add(addr);

                CallInst instance = new CallInst(curBasicBlock, CalledFunc, args);
                curBasicBlock.AddInstAtTail(instance);
                logger.fine("IR build for '" + CalledFunc.getIdentifier() + "' method call done.", node.GetLocation());
                return instance;

            } else {
                CalledFunc = TopModule.getFunctionMap().get(
                        mx_func.getClassName() + '.' + mx_func.getIdentifier()
                );
            }
            assert CalledFunc != null;
            // TODO add method call
        } else {
            Function CalledFunc = TopModule.getFunctionMap().get(mx_func.getIdentifier());
            if (CalledFunc == null) {
                logger.severe("Fatal error, no such function, check func init.", node.GetLocation());
                System.exit(1);
            }
            ArrayList<Value> args = new ArrayList<>();

            for (ExprNode expr : node.getParameters()) {
                Value arg = (Value) expr.accept(this);
                args.add(arg);
            }

            CallInst instance = new CallInst(curBasicBlock, CalledFunc, args);
            curBasicBlock.AddInstAtTail(instance);
            logger.fine("IR build for '" + CalledFunc.getIdentifier() + "' function call done.", node.GetLocation());
            return instance;
        }
        return null;
    }

    @Override
    public Object visit(ParameterNode node) {
        return null;
    }

    @Override
    public Object visit(SemiStmtNode node) {
        return null;
    }
}
