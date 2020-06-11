package BackEnd;

import AST.*;
import Frontend.Scope;
import IR.*;
import IR.Constants.*;
import IR.Instructions.*;
import IR.Module;
import IR.Types.IRBaseType;
import IR.Types.PointerType;
import IR.Types.StructureType;
import MxEntity.ClassEntity;
import MxEntity.FunctionEntity;
import Tools.Location;
import Tools.MXLogger;
import Tools.Operators;
import org.antlr.v4.codegen.model.Loop;

import java.awt.image.BandedSampleModel;
import java.util.ArrayList;
import java.util.Stack;

public class IRBuilder implements ASTVisitor {
    private static Module TopModule;
    private Scope GlobalScope;
    private Function curFunction, init;
    private BasicBlock curBasicBlock, curLoopBlock;
    private String curClassName;
    static public MXLogger logger;

    private Stack<BasicBlock> MergeStackForBreak;
    private Stack<BasicBlock> LoopStackForContinue;
    private Stack<ValueSymbolTable> EnteredTable;
    ValueSymbolTable LocalSymTab;
    private boolean usei64Array; // set true if we want runnable IR
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
        MergeStackForBreak = new Stack<>();
        LoopStackForContinue = new Stack<>();
        EnteredTable = new Stack<>();
        LocalSymTab = new ValueSymbolTable();
        init = new Function("_entry_block", Module.VOID, new ArrayList<>(), false);
        TopModule.defineFunction(init);
        init.initialize();
        this.usei64Array = false;
        logger.info("IRBuild ready to start.");
    }

    public IRBuilder(Scope globalScope, MXLogger logger, boolean i64Array) {
        this(globalScope, logger);
        this.usei64Array = i64Array;
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
            return Module.ADDR;
        } else if (type.isClass()) {
            if (TopModule.getClassMap().containsKey(type.getName())) {
                return new PointerType(TopModule.getClassMap().get(type.getName()));
            } else {
                return new PointerType(new StructureType(type.getName()));
            }
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
        if (!curBasicBlock.endWithBranch())
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
                    if (initValue instanceof NullConst)
                        initValue.setType(type);
                    curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, initValue, AllocaAddr));
                }
                logger.fine("IR build for '" + subnode.getIdentifier() + "' variable declaration ir done.");
            }

        }

        return null;
    }

    @Override
    public Object visit(ClassDecNode node) {
        if (node.getAcceptStage() == 0) {
            ArrayList<IRBaseType> MemberList = new ArrayList<>();
            ArrayList<String> NameList = new ArrayList<>();
            for (VariableDecNode subnode : node.getVarNodeList()) {
                IRBaseType memberType = ConvertTypeFromAST(subnode.getType());
                assert memberType != null;

                for (VarDecoratorNode var : subnode.getVarDecoratorList()) {
                    MemberList.add(memberType);
                    NameList.add(var.getIdentifier());
                }
            }
            StructureType curClass = new StructureType(node.getIdentifier(), MemberList, NameList);
            TopModule.defineClass(node.getIdentifier(), curClass);
        } else if (node.getAcceptStage() == 1) {
            StructureType curClass = TopModule.getClassMap().get(node.getIdentifier());
            curClassName = node.getIdentifier();

            for (int i = 0; i < curClass.getMemberTypeList().size(); i ++) {
                IRBaseType type = curClass.getMemberTypeList().get(i);
                if (type instanceof PointerType) {
                    PointerType p = (PointerType) type;
                    IRBaseType baseType = p.getBaseType();
                    if (baseType instanceof StructureType) {
                        if (((StructureType) baseType).isFakeType()  ) {
                            String className = ((StructureType) baseType).getIdentifier();
                            StructureType realType = TopModule.getClassMap().get(className);
                            p.setBaseType(realType);
                        }
                    }
                }
            }

            for (MethodDecNode method : node.getMethodNodeList()) {
                method.accept(this);
            }
            curClassName = null;
        }
        return null;
    }

    @Override
    public Object visit(MethodDecNode node) {
        Function method = null;
        if (curClassName == null) {
            logger.severe("method visit outside of class", node.GetLocation());
            logger.severe("Exit");
            System.exit(1);
        }
        String methodName = curClassName + "." + node.getIdentifier();
        if (TopModule.getFunctionMap().containsKey(methodName)) {
            method = TopModule.getFunctionMap().get(methodName);
            curFunction = method;
        } else {
            logger.severe("No such method called: " + methodName, node.GetLocation());
            logger.severe("Exit");
            System.exit(1);
        }

        curBasicBlock = method.getHeadBlock();
        curFunction.setThisExpr(method.getVarSymTab().get("this"));
        EnterScope(method);
        node.getFuncBlock().accept(this);
        if (!curBasicBlock.endWithBranch())
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, method.getRetBlock(), null));
        method.AddBlockAtTail(method.getRetBlock());
        ExitScope();
        curFunction = null;
        curBasicBlock = null;
        logger.fine("IR build for method " + methodName + " done", node.GetLocation());
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

        if (!condition.getType().isIntegerType()) {
            logger.severe("Condition is not boolean type", node.GetLocation());
        }
        if (node.isHasElse()) {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, condition, ThenBlock, ElseBlock));
        } else {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, condition, ThenBlock, MergeBlock));
        }
        curBasicBlock = ThenBlock;
        curFunction.AddBlockAtTail(ThenBlock);

        node.getThenStmt().accept(this); // curBasicBlock may change here
        boolean useMergeBlock = false;
        if (!curBasicBlock.endWithBranch()) {
            // else is the return inst
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, MergeBlock, null));
            useMergeBlock = true;
        }


        if (node.isHasElse()) {
            curBasicBlock = ElseBlock;
            node.getElseStmt().accept(this);
            if (!curBasicBlock.endWithBranch()) {
                curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, MergeBlock, null));
                useMergeBlock = true;
            }
            curFunction.AddBlockAtTail(ElseBlock);
        }
        // if (useMergeBlock) {
            curBasicBlock = MergeBlock;
            curFunction.AddBlockAtTail(MergeBlock);
        // }
        return null;
    }

    @Override
    public Object visit(BreakStmtNode node) {
        curBasicBlock.AddInstAtTail(new BranchInst(
                curBasicBlock, null, MergeStackForBreak.peek(), null
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
        curFunction.AddBlockAtTail(CondBlock);
        Value cond = (Value) node.getCondition().accept(this);
        if (cond instanceof BoolConst) {
            if (((BoolConst) cond).constValue == 1)
                curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, LoopBody, null));
            else
                curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, MergeBlock, null));
        } else curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, cond, LoopBody, MergeBlock));
        LoopStackForContinue.push(CondBlock);
        curFunction.AddBlockAtTail(LoopBody);

        MergeStackForBreak.push(MergeBlock);
        curBasicBlock = LoopBody;
        node.getLoopStmt().accept(this);
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, CondBlock, null));
        curBasicBlock = MergeBlock;
        curFunction.AddBlockAtTail(MergeBlock);

        LoopStackForContinue.pop();
        MergeStackForBreak.pop();

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

        curFunction.AddBlockAtTail(CondBlock);
        curBasicBlock = CondBlock;
        if (node.getCondExpr() != null) {
            Instruction condInst = (Instruction) node.getCondExpr().accept(this);
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, condInst, LoopBody, MergeBlock));
            LoopStackForContinue.push(UpdateBlock);
            MergeStackForBreak.push(MergeBlock);
        } else {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, LoopBody, null));
            LoopStackForContinue.push(UpdateBlock);
            MergeStackForBreak.push(MergeBlock);
        }
        curFunction.AddBlockAtTail(LoopBody);
        curBasicBlock = LoopBody;
        node.getLoopBlcok().accept(this);
        if (!curBasicBlock.endWithBranch()) {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, UpdateBlock, null));
        }
        LoopStackForContinue.pop();
        MergeStackForBreak.pop();

        curFunction.AddBlockAtTail(UpdateBlock);
        curBasicBlock = UpdateBlock;
        if (node.getUpdateExpr() != null)
            node.getUpdateExpr().accept(this);
        // could only be assign expr
        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, CondBlock, null));

        curFunction.AddBlockAtTail(MergeBlock);
        curBasicBlock = MergeBlock;
        return null;
    }

    @Override
    public Object visit(ReturnStmtNode node) {
        if (node.getReturnedExpr() != null) {
            Value RetValue;
            if (node.getReturnedExpr() instanceof ConstNode) {
                boolean old_left = isLeftValue;
                isLeftValue = false;
                RetValue = (Value) node.getReturnedExpr().accept(this);
                if (!(RetValue instanceof Constant)) {
                    logger.fine("return string constant", node.GetLocation());
                }
                isLeftValue = old_left;
                if (RetValue instanceof NullConst) {
                    IRBaseType retType = curFunction.getRetValue().getType();
                    retType = ((PointerType) retType).getBaseType();
                    RetValue.setType(retType);
                }
            } else {
                boolean old_left = isLeftValue;
                isLeftValue = false;
                RetValue = (Value) node.getReturnedExpr().accept(this);
                isLeftValue = old_left;
            }
            curBasicBlock.AddInstAtTail(new StoreInst(curBasicBlock, RetValue, curFunction.getRetValue()));
        }
        if (!curBasicBlock.endWithBranch()) {
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, curFunction.getRetBlock(), null));
        }
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
        if (node.getType().isNull()) {
            return new NullConst(constType);
        }

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
        baseType.setArrayLevel(arrayLevel - 1);
        IRBaseType arrayBaseType = ConvertTypeFromAST(baseType);

        return getNewArray(arrayBaseType, arrayLevel, sizeList, node.GetLocation());
    }

    private Value getNewArray(IRBaseType arrayBaseType, int arrayLevel, ArrayList<Value> arraySizeList, Location location) {
        int t = (arrayBaseType instanceof PointerType) ? 8 : 4;
        int arrayUnitSize = (arrayLevel > 1) ? 8 : t;
        Value arraySize = arraySizeList.get(0);
        BinOpInst malloc_size = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.mul, arraySize, new IntConst(arrayUnitSize));
        BinOpInst total_size = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.add, malloc_size, new IntConst(8));
        // SextInst malloc_size_i64 = new SextInst(curBasicBlock, Module.I32, total_size, Module.I64);

        ArrayList<Value> paras = new ArrayList<>();
        // paras.add(malloc_size_i64);
        paras.add(total_size);
        Function _malloc = TopModule.getFunctionMap().get("_malloc_and_init");
        CallInst malloc_addr = new CallInst(curBasicBlock, _malloc, paras);
        curBasicBlock.AddInstAtTail(malloc_size);
        curBasicBlock.AddInstAtTail(total_size);
        // curBasicBlock.AddInstAtTail(malloc_size_i64);
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
        if (usei64Array)
            offsets.add(new IntConst(8 / arrayUnitSize));
        else
            offsets.add(new IntConst(1));

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

        curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, condBlock, null));
        curBasicBlock = condBlock;
        LoadInst subArray = new LoadInst(curBasicBlock, subArrayType, subArrayAddr);
        CmpInst subArrayReachTail = new CmpInst(curBasicBlock, Operators.BinaryOp.LESS, subArray,
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
        LoadInst subArray_1 = new LoadInst(curBasicBlock, subArrayType, subArrayAddr);
        GetPtrInst nextSubArray = new GetPtrInst(curBasicBlock, subArray_1, offsets, arrayBaseType);
        StoreInst stNextSubArray = new StoreInst(curBasicBlock, nextSubArray, subArrayAddr);
        curBasicBlock.AddInstAtTail(subArray_1);
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

        // malloc some space, and bitcast to (class*)
        Function malloc = TopModule.getFunctionMap().get("malloc");
        ArrayList<Value> paras = new ArrayList<>();
        paras.add(new IntConst(classType.getBytes()));
        CallInst mallocCall = new CallInst(curBasicBlock, malloc, paras);
        BitCastInst addr = new BitCastInst(curBasicBlock, mallocCall,
                new PointerType(classType));
        curBasicBlock.AddInstAtTail(mallocCall);
        curBasicBlock.AddInstAtTail(addr);

        if (TopModule.getFunctionMap().containsKey(name + '.' + name)) {
            // has constructor
            Function constructor = TopModule.getFunctionMap().get(name + '.' + name);
            paras = new ArrayList<>();
            paras.add(addr);
            CallInst funcCall = new CallInst(curBasicBlock, constructor, paras);
            curBasicBlock.AddInstAtTail(funcCall);
        }
        return addr;

    }

    public Object visitLogicAnd(BinExprNode node) {

        boolean old_left = isLeftValue;
        isLeftValue = false;
        Value LHS = (Value) node.getLeftExpr().accept(this);

        if (!LHS.getType().equals(Module.I1)) {
            logger.warning("Use logic and on non bool type.", node.GetLocation());
            System.exit(1);
        } else {
            BasicBlock lhsBlock = curBasicBlock;
            BasicBlock rhsBlock = new BasicBlock(curFunction, "LogicAndRHS");
            BasicBlock mergeBlock = new BasicBlock(curFunction, "LogicAndMerge");
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, LHS, rhsBlock, mergeBlock));
            curBasicBlock = rhsBlock;
            Value RHS = (Value) node.getRightExpr().accept(this);
            isLeftValue = old_left;

            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, mergeBlock, null));
            curBasicBlock = mergeBlock;
            PhiInst phi = new PhiInst(curBasicBlock, Module.I1);
            phi.AddPhiBranch(lhsBlock, LHS);
            phi.AddPhiBranch(rhsBlock, RHS);
            curBasicBlock.AddInstAtTail(phi);
            curFunction.AddBlockAtTail(rhsBlock);
            curFunction.AddBlockAtTail(mergeBlock);
            return phi;
        }
        return null;
    }

    public Object visitLogicOr(BinExprNode node) {
        boolean old_left = isLeftValue;
        isLeftValue = false;
        Value LHS = (Value) node.getLeftExpr().accept(this);

        if (!LHS.getType().equals(Module.I1)) {
            logger.warning("Use logic or on non bool type.", node.GetLocation());
            System.exit(1);
        } else {
            BasicBlock lhsBlock = curBasicBlock;
            BasicBlock rhsBlock = new BasicBlock(curFunction, "LogicOrRHS");
            BasicBlock mergeBlock = new BasicBlock(curFunction, "LogicOrMerge");
            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, LHS, mergeBlock, rhsBlock));
            curBasicBlock = rhsBlock;
            Value RHS = (Value) node.getRightExpr().accept(this);
            isLeftValue = old_left;

            curBasicBlock.AddInstAtTail(new BranchInst(curBasicBlock, null, mergeBlock, null));
            curBasicBlock = mergeBlock;
            PhiInst phi = new PhiInst(curBasicBlock, Module.I1);
            phi.AddPhiBranch(lhsBlock, LHS);
            phi.AddPhiBranch(rhsBlock, RHS);
            curBasicBlock.AddInstAtTail(phi);
            curFunction.AddBlockAtTail(rhsBlock);
            curFunction.AddBlockAtTail(mergeBlock);
            return phi;
        }
        return null;
    }

    @Override
    public Object visit(BinExprNode node) {
        Operators.BinaryOp bop = node.getBop();
        switch (bop) {
            case LOGIC_AND: {
                return visitLogicAnd(node);
            }
            case LOGIC_OR: {
                return visitLogicOr(node);
            }
            default:
                break;
        }

        boolean old_left = isLeftValue;
        isLeftValue = bop.equals(Operators.BinaryOp.ASSIGN);
        Value LHS = (Value) node.getLeftExpr().accept(this);
        if (LHS instanceof GetPtrInst) {
            if (!isLeftValue && !(node.getLeftExpr() instanceof ConstNode)) {
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
        if (RHS instanceof GetPtrInst) {
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
        boolean constantFold = LHS instanceof IntConst && RHS instanceof IntConst;
        int lValue = 0, rValue = 0;
        if (constantFold) {
            lValue = ((IntConst) LHS).ConstValue;
            rValue = ((IntConst) RHS).ConstValue;
        }
        switch (bop) {
            case ADD: {
                if (LHS.getType().equals(Module.I32)) {
                    if (constantFold) {
                        return new IntConst(lValue + rValue);
                    }
                    BinOpInst instance = new BinOpInst(curBasicBlock, Module.I32, Instruction.InstType.add, LHS, RHS);
                    curBasicBlock.AddInstAtTail(instance);
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
                } else {
                    logger.warning("Add unknown type" + LHS.getType().toString(), node.GetLocation());
                    System.exit(1);
                }
                break;
            }
            case SUB: {
                // only integer sub
                if (!LHS.getType().equals(Module.I32)) {
                    logger.severe("Use sub on non integer type.", node.GetLocation());
                    System.exit(1);
                } else {
                    if (constantFold) {
                        return new IntConst(lValue - rValue);
                    }
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
                    if (constantFold) {
                        return new IntConst(lValue * rValue);
                    }
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
                    if (constantFold) {
                        return new IntConst(lValue / rValue);
                    }
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
                    if (constantFold) {
                        return new IntConst(lValue % rValue);
                    }
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
                    if (constantFold) {
                        return new IntConst(lValue << rValue);
                    }
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
                    if (constantFold) {
                        return new IntConst(lValue >> rValue);
                    }
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
                if (!LHS.getType().equals(Module.I32) && !(RHS instanceof NullConst) ) {
                    logger.warning("Use ge or equal on non integer type.", node.GetLocation());
                } else {
                    CmpInst instance = new CmpInst(curBasicBlock, bop, LHS, RHS);
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
            case LOGIC_AND:
            case LOGIC_OR: {
                logger.severe("Fatal error", node.GetLocation());
                System.exit(1);
            }
            case ASSIGN: {
                // RHS may be const or inst
                // LHS must be address (alloca)
                if (RHS instanceof NullConst) {
                    IRBaseType nullType = ((PointerType) LHS.getType()).getBaseType();
                    RHS.setType(nullType);
                }
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
                if (curClassName != null) {

                    StructureType curClass = TopModule.getClassMap().get(curClassName);
                    IRBaseType memberType = curClass.getElementType(node.getIdentifier());
                    if (memberType == null) {
                        logger.severe("Cannot fetch member type!", node.GetLocation());
                        System.exit(1);
                    }
                    Value thisPtr = curFunction.getThisExpr();
                    thisPtr = new LoadInst(curBasicBlock, new PointerType(curClass), thisPtr);
                    curBasicBlock.AddInstAtTail((Instruction) thisPtr);
                    int index = curClass.getMemberOffset(node.getIdentifier());
                    ArrayList<Value> offsets = new ArrayList<>();
                    offsets.add(new IntConst(0));
                    offsets.add(new IntConst(index));
                    GetPtrInst memberPtr = new GetPtrInst(curBasicBlock, thisPtr, offsets, memberType);
                    curBasicBlock.AddInstAtTail(memberPtr);
                    if (isLeftValue)
                        return memberPtr;
                    else {
                        LoadInst memberInstance = new LoadInst(curBasicBlock, memberType, memberPtr);
                        curBasicBlock.AddInstAtTail(memberInstance);
                        return memberInstance;
                    }
                }


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
            // size & some built-in methods
            return node.getExpr().accept(this);
        }

        boolean old_left = isLeftValue;
        isLeftValue = false;
        Value ExprPointer = (Value) node.getExpr().accept(this);
        isLeftValue = old_left;

        if (node.getExpr() instanceof ThisExprNode && ExprPointer == null) {
            ExprPointer = curFunction.getThisExpr();
            ExprPointer = new LoadInst(curBasicBlock, ((AllocaInst) ExprPointer).getBaseType(), ExprPointer);
            curBasicBlock.AddInstAtTail((Instruction) ExprPointer);
        }

        if (memberNotNeeded) {
            // when we call a method in another method
            return ExprPointer;
        }
        // what about method here ??? return getExpr directly
        StructureType classStructure = TopModule.getClassMap().get(classType.getName());
        ClassEntity classEntity = GlobalScope.GetClass(classType.getName());
        int offset = classStructure.getMemberOffset(node.getMember());
        if (offset == -1) {
            logger.severe("No such member type found", node.GetLocation());
        }
        GetPtrInst memberPtr;

        ArrayList<Value> offsets = new ArrayList<>();
        offsets.add(new IntConst(0));
        offsets.add(new IntConst(offset));
        IRBaseType eleType = classStructure.getElementType(offset);
        memberPtr = new GetPtrInst(curBasicBlock, ExprPointer, offsets, eleType);
        curBasicBlock.AddInstAtTail(memberPtr);

        if (!isLeftValue) {
            LoadInst memberInstance = new LoadInst(curBasicBlock, memberPtr.getElementType(), memberPtr);
            curBasicBlock.AddInstAtTail(memberInstance);
            return memberInstance;
        }

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
                return res;
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
                // if ()
                return res;
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
        Value ExprPointer = curFunction.getThisExpr();
        ExprPointer = new LoadInst(curBasicBlock, ((AllocaInst) ExprPointer).getBaseType(), ExprPointer);
        curBasicBlock.AddInstAtTail((Instruction) ExprPointer);
        return ExprPointer;
    }

    private boolean memberNotNeeded;

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
                if (CalledFunc == null) System.exit(1);
                Value class_instance;
                boolean old_left = isLeftValue;
                isLeftValue = false;

                if (node.getObj() instanceof MemberExprNode) {
                    boolean old_member = memberNotNeeded;
                    memberNotNeeded = true;
                    class_instance = (Value) node.getObj().accept(this);
                    memberNotNeeded = old_member;
                } else {
                    class_instance = curFunction.getThisExpr();
                    class_instance = new LoadInst(curBasicBlock, ((AllocaInst)class_instance).getBaseType(), class_instance);
                    curBasicBlock.AddInstAtTail((Instruction) class_instance);
                }
                ArrayList<Value> args = new ArrayList<>();
                args.add(class_instance);
                for (ExprNode expr : node.getParameters()) {
                    Value arg = (Value) expr.accept(this);
                    args.add(arg);
                }
                isLeftValue = old_left;

                CallInst methodCall = new CallInst(curBasicBlock, CalledFunc, args);
                curBasicBlock.AddInstAtTail(methodCall);
                return methodCall;

            }

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
