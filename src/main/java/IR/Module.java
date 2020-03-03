package IR;

import AST.FunctionDecNode;
import AST.MethodDecNode;
import AST.ParameterNode;
import BackEnd.IRBuilder;
import IR.Instructions.AllocaInst;
import IR.Instructions.StoreInst;
import IR.Types.*;
import Tools.MXLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * LLVM programs are composed of Module’s, each of which is a translation unit of the input programs.
 * Each module consists of functions, global variables, and symbol table entries.
 * Modules may be combined together with the LLVM linker, which merges function (and global variable) definitions,
 * resolves forward declarations, and merges symbol table entries.

 * A Module instance is used to store all the information related to an
 * LLVM module. Modules are the top level container of all other LLVM
 * Intermediate Representation (IR) objects. Each module directly contains a
 * list of globals variables, a list of functions, a list of libraries (or
 * other modules) this module depends on, a symbol table, and various data
 * about the target's characteristics.
 */
public class Module extends Value{
    private HashMap<String, GlobalVariable> GlobalVarMap;
    private HashMap<String, Function> FunctionMap;
    private HashMap<String, StructureType> ClassMap;
    private HashMap<String, BasicBlock> LabelMap;
    private ValueSymbolTable VarSymTab;
    public String TargetTriple;
    public String SourceFileName;
    public String ModuleID;
    public String TargetDataLayout;
    private MXLogger logger;

    public static final VoidType VOID = new VoidType();
    public static final IntegerType I1 = new IntegerType(IntegerType.BitWidth.i1);
    public static final IntegerType I8 = new IntegerType(IntegerType.BitWidth.i8);
    public static final IntegerType I16 = new IntegerType(IntegerType.BitWidth.i16);
    public static final IntegerType I32 = new IntegerType(IntegerType.BitWidth.i32);
    public static final PointerType STRING = new PointerType(I8);


    public Module(ValueSymbolTable varSymTab, MXLogger logger) {
        super(ValueType.MODULE);
        this.VarSymTab = varSymTab;
        this.logger = logger;
        GlobalVarMap = new HashMap<>();
        FunctionMap = new HashMap<>();
        ClassMap = new HashMap<>();
        TargetTriple = "x86_64-pc-linux-gnu";
        TargetDataLayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128";
        ModuleID = "Basic1";
        SourceFileName = "Basic1.cpp";

        PreProcess();
    }

    public void RefreshClassMapping() {
        for (StructureType structureType : ClassMap.values()) {
            structureType.getMemberList().forEach(memberType->{
                if (memberType.getBaseTypeName().equals(IRBaseType.TypeID.StructTyID)) {
                    String className = ((StructureType) memberType).getIdentifier();
                    memberType = ClassMap.get(className);
                }
            });
        }
    }

    private void PreProcess() {
        // Add external function to map, these functions only need to print
        // something like `declare i32 @printf(i8*, ...) #2`
        // print(string str)
        ArrayList<Argument> print_paras = new ArrayList<>();
        print_paras.add(new Argument(null, STRING, 0));
        Function _print = new Function("print", VOID, print_paras, true);
        FunctionMap.put(_print.getIdentifier(), _print);
        // println(string str) -> end with '/n'
        ArrayList<Argument> println_paras = new ArrayList<>();
        println_paras.add(new Argument(null, STRING, 0));
        Function _println = new Function("println", VOID, println_paras, true);
        FunctionMap.put(_println.getIdentifier(), _println);
        // printlnInt(int n) -> end with '/n'
        ArrayList<Argument> printlnInt_paras = new ArrayList<>();
        printlnInt_paras.add(new Argument(null, I32, 0));
        Function _printlnInt = new Function("printlnInt", VOID, printlnInt_paras, true);
        FunctionMap.put(_printlnInt.getIdentifier(), _printlnInt);
        // toString(int i)
        ArrayList<Argument> toString_paras = new ArrayList<>();
        toString_paras.add(new Argument(null, I32, 0));
        Function _toString = new Function("toString", STRING, toString_paras, true);
        FunctionMap.put(_toString.getIdentifier(), _toString);
        // String add
        ArrayList<Argument> StringAdd_paras = new ArrayList<>();
        StringAdd_paras.add(new Argument(null, STRING, 0));
        StringAdd_paras.add(new Argument(null, STRING, 1));
        Function _StringAdd = new Function("_string_add", STRING, StringAdd_paras, true);
        FunctionMap.put(_StringAdd.getIdentifier(), _StringAdd);
        // getInt()
        ArrayList<Argument> getInt_paras = new ArrayList<>();
        Function _getInt = new Function("getInt", I32, getInt_paras, true);
        FunctionMap.put(_getInt.getIdentifier(), _getInt);
        // printInt()
        ArrayList<Argument> printInt_paras = new ArrayList<>();
        printInt_paras.add(new Argument(null, I32, 0));
        Function _printInt = new Function("printInt", VOID, printInt_paras, true);
        FunctionMap.put(_printInt.getIdentifier(), _printInt);
    }

    public HashMap<String, Function> getFunctionMap() {
        return FunctionMap;
    }

    public HashMap<String, GlobalVariable> getGlobalVarMap() {
        return GlobalVarMap;
    }

    public HashMap<String, StructureType> getClassMap() {
        return ClassMap;
    }

    public ValueSymbolTable getVarSymTab() {
        return VarSymTab;
    }

    public void defineLabel(BasicBlock basicBlock) { LabelMap.put(basicBlock.getLabel(), basicBlock); }

    public void defineFunction(Function func) {
        FunctionMap.put(func.getIdentifier(), func);
    }

    public void defineFunction(MethodDecNode methodDecNode, String ClassName) {
        // TODO Deal with method (first para is this)
        String methodName = ClassName + '.' + methodDecNode.getIdentifier();
        IRBaseType RetType;
        if (methodDecNode.isConstructMethod()) {
            RetType = ClassMap.get(ClassName);
        } else {
            RetType = IRBuilder.ConvertTypeFromAST(methodDecNode.getReturnType().getType());
        }
        ArrayList<Argument> args = new ArrayList<>();
        int id = 1;
        Argument pointerThis = new Argument(null, new PointerType(ClassMap.get(ClassName)), 0);
        args.add(pointerThis);
        for (ParameterNode para : methodDecNode.getParaDecList()) {
            Argument arg = new Argument(null, IRBuilder.ConvertTypeFromAST(para.getType()), id);
            args.add(arg);
            id += 1;
        }
        Function method = new Function(methodName, RetType, args, false);
        method.initialize();
        BasicBlock head = method.getHeadBlock();
        ArrayList<AllocaInst> AllocaList = new ArrayList<>();
        ArrayList<StoreInst> StoreList = new ArrayList<>();

        for (Argument arg : method.getParameterList()) {
            AllocaInst ArgAddr = new AllocaInst(head, arg.type);
            AllocaList.add(ArgAddr);
            StoreInst storeInst = new StoreInst(head, arg, ArgAddr);
            StoreList.add(storeInst);
        }

        for (StoreInst st : StoreList) {
            head.AddInstAtTop(st);
        }
        for (AllocaInst al : AllocaList) {
            head.AddInstAtTop(al);
        }
        FunctionMap.put(methodName, method);
        logger.info("IR Module, initialize method: " + methodName);
    }

    public void defineFunction(FunctionDecNode FuncDecNode) {
        String FuncName = FuncDecNode.getIdentifier();
        IRBaseType RetType = IRBuilder.ConvertTypeFromAST(FuncDecNode.getReturnType().getType() );
        ArrayList<Argument> args = new ArrayList<>();
        int id = 0;
        for (ParameterNode para : FuncDecNode.getParaDecList()) {
            Argument arg = new Argument(null, IRBuilder.ConvertTypeFromAST(para.getType()), id);
            args.add(arg);
            id += 1;
        }
        Function function = new Function(FuncName, RetType, args, false);
        function.initialize();
        BasicBlock head = function.getHeadBlock();
        // Allocate address for all the arguments
        ArrayList<AllocaInst> AllocaList = new ArrayList<>();
        ArrayList<StoreInst> StoreList = new ArrayList<>();
        for (Argument arg : function.getParameterList()) {
            AllocaInst ArgAddr = new AllocaInst(head, arg.type);
            AllocaList.add(ArgAddr);
            StoreInst storeInst = new StoreInst(head, arg, ArgAddr);
            StoreList.add(storeInst);
        }

        for (StoreInst st : StoreList) {
            head.AddInstAtTop(st);
        }
        for (AllocaInst al : AllocaList) {
            head.AddInstAtTop(al);
        }

        FunctionMap.put(FuncName, function);
    }

    public void defineClass(String name, StructureType structureType) {
        ClassMap.put(name, structureType);
    }

    public void defineGlobalVar(GlobalVariable globalVariable) {
        GlobalVarMap.put(globalVariable.getIdentifier(), globalVariable);
    }

    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) throws IOException {
        visitor.visit(this);
    }

    public HashMap<String, BasicBlock> getLabelMap() {
        return LabelMap;
    }
}
