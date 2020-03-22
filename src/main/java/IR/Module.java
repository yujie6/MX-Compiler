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
import java.util.List;
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
    private HashMap<String, Value> GlobalVarMap;
    private HashMap<String, Function> FunctionMap;
    private HashMap<String, StructureType> ClassMap;
    private HashMap<String, BasicBlock> LabelMap;
    private ValueSymbolTable VarSymTab;
    private HashMap<String, GlobalVariable> StringConstMap;
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
    public static final IntegerType I64 = new IntegerType(IntegerType.BitWidth.i64);
    public static final PointerType STRING = new PointerType(I8);
    public static final PointerType ADDR = new PointerType(I8);


    public Module(ValueSymbolTable varSymTab, MXLogger logger) {
        super(ValueType.MODULE);
        this.VarSymTab = varSymTab;
        this.logger = logger;
        GlobalVarMap = new HashMap<>();
        FunctionMap = new HashMap<>();
        ClassMap = new HashMap<>();
        StringConstMap = new HashMap<>();
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
        print_paras.add(new Argument(null, STRING, 0, "str"));
        Function _print = new Function("print", VOID, print_paras, true);
        FunctionMap.put(_print.getIdentifier(), _print);
        // println(string str) -> end with '/n'
        ArrayList<Argument> println_paras = new ArrayList<>();
        println_paras.add(new Argument(null, STRING, 0, "str"));
        Function _println = new Function("println", VOID, println_paras, true);
        FunctionMap.put(_println.getIdentifier(), _println);
        // printlnInt(int n) -> end with '/n'
        ArrayList<Argument> printlnInt_paras = new ArrayList<>();
        printlnInt_paras.add(new Argument(null, I32, 0, "str"));
        Function _printlnInt = new Function("printlnInt", VOID, printlnInt_paras, true);
        FunctionMap.put(_printlnInt.getIdentifier(), _printlnInt);
        // toString(int i)
        ArrayList<Argument> toString_paras = new ArrayList<>();
        toString_paras.add(new Argument(null, I32, 0, "i"));
        Function _toString = new Function("toString", STRING, toString_paras, true);
        FunctionMap.put(_toString.getIdentifier(), _toString);
        // getInt()
        ArrayList<Argument> getInt_paras = new ArrayList<>();
        Function _getInt = new Function("getInt", I32, getInt_paras, true);
        FunctionMap.put(_getInt.getIdentifier(), _getInt);
        // printInt()
        ArrayList<Argument> printInt_paras = new ArrayList<>();
        printInt_paras.add(new Argument(null, I32, 0, "i"));
        Function _printInt = new Function("printInt", VOID, printInt_paras, true);
        FunctionMap.put(_printInt.getIdentifier(), _printInt);

        // malloc()
        ArrayList<Argument> malloc_paras = new ArrayList<>();
        malloc_paras.add(new Argument(null, I64, 0, "size"));
        Function _malloc = new Function("malloc", ADDR, malloc_paras, true);
        FunctionMap.put(_malloc.getIdentifier(), _malloc);
        // getString()
        ArrayList<Argument> getString_paras = new ArrayList<>();
        Function _getString = new Function("getString", STRING, getString_paras, true);
        FunctionMap.put(_getString.getIdentifier(), _getString);


        DefineStringFunction();

    }

    private void DefineStringFunction() {
        // __string_length
        ArrayList<Argument> string_len_paras = new ArrayList<>();
        string_len_paras.add(new Argument(null, STRING, 0, "str"));
        Function __string_length = new Function("__string_length", I32, string_len_paras, true);
        FunctionMap.put(__string_length.getIdentifier(), __string_length);
        // __string_substring
        ArrayList<Argument> string_substring_paras = new ArrayList<>();
        string_substring_paras.add(new Argument(null, STRING, 0, "str"));
        string_substring_paras.add(new Argument(null, I32, 1, "left"));
        string_substring_paras.add(new Argument(null, I32, 2, "right"));
        Function __string_substring = new Function("__string_substring", STRING, string_substring_paras, true);
        FunctionMap.put(__string_substring.getIdentifier(), __string_substring);
        // __string_concatenate
        ArrayList<Argument> string_concatenate_paras = new ArrayList<>();
        string_concatenate_paras.add(new Argument(null, STRING, 0, "str1"));
        string_concatenate_paras.add(new Argument(null, STRING, 1, "str2"));
        Function __string_concatenate = new Function("__string_concatenate", STRING, string_concatenate_paras, true);
        FunctionMap.put(__string_concatenate.getIdentifier(), __string_concatenate);
        // __string_equal
        ArrayList<Argument> string_equal_paras = new ArrayList<>();
        string_equal_paras.add(new Argument(null, STRING, 0, "str1"));
        string_equal_paras.add(new Argument(null, STRING, 1, "str2"));
        Function __string_equal = new Function("__string_equal", I1, string_equal_paras, true);
        FunctionMap.put(__string_equal.getIdentifier(), __string_equal);
        // __string_notEqual
        ArrayList<Argument> string_notequal_paras = new ArrayList<>();
        string_notequal_paras.add(new Argument(null, STRING, 0, "str1"));
        string_notequal_paras.add(new Argument(null, STRING, 1, "str2"));
        Function __string_notEqual = new Function("__string_notEqual", I1, string_notequal_paras, true);
        FunctionMap.put(__string_notEqual.getIdentifier(), __string_notEqual);
        // __string_lessThan
        ArrayList<Argument> string_lessthan_paras = new ArrayList<>();
        string_lessthan_paras.add(new Argument(null, STRING, 0, "str1"));
        string_lessthan_paras.add(new Argument(null, STRING, 1, "str2"));
        Function __string_lessThan = new Function("__string_lessThan", I1, string_lessthan_paras, true);
        FunctionMap.put(__string_lessThan.getIdentifier(), __string_lessThan);
        // __string_greaterThan
        ArrayList<Argument> string_greaterThan_paras = new ArrayList<>();
        string_greaterThan_paras.add(new Argument(null, STRING, 0, "str1"));
        string_greaterThan_paras.add(new Argument(null, STRING, 1, "str2"));
        Function __string_greaterThan= new Function("__string_greaterThan", I1, string_greaterThan_paras, true);
        FunctionMap.put(__string_greaterThan.getIdentifier(), __string_greaterThan);
        // __string_lessEqual
        ArrayList<Argument> string_lessEqual_paras = new ArrayList<>();
        string_lessEqual_paras.add(new Argument(null, STRING, 0, "str1"));
        string_lessEqual_paras.add(new Argument(null, STRING, 1, "str2"));
        Function __string_lessEqual= new Function("__string_lessEqual", I1, string_lessEqual_paras, true);
        FunctionMap.put(__string_lessEqual.getIdentifier(), __string_lessEqual);
        // __string_greaterEqual
        ArrayList<Argument> string_greaterEqual_paras = new ArrayList<>();
        string_greaterEqual_paras.add(new Argument(null, STRING, 0, "str1"));
        string_greaterEqual_paras.add(new Argument(null, STRING, 1, "str2"));
        Function __string_greaterEqual= new Function("__string_greaterEqual", I1, string_greaterEqual_paras, true);
        FunctionMap.put(__string_greaterEqual.getIdentifier(), __string_greaterEqual);

        // __string_ord
        ArrayList<Argument> string_ord_paras = new ArrayList<>();
        string_ord_paras.add(new Argument(null, STRING, 0, "str"));
        string_ord_paras.add(new Argument(null, I32, 1, "pos"));
        Function __string_ord = new Function("__string_ord", I32, string_ord_paras, true);
        FunctionMap.put(__string_ord.getIdentifier(), __string_ord);
        // __string_parseInt
        ArrayList<Argument> string_parseInt_paras = new ArrayList<>();
        string_parseInt_paras.add(new Argument(null, STRING,  0, "str"));
        Function __string_parseInt = new Function("__string_parseInt", I32, string_parseInt_paras, true);
        FunctionMap.put(__string_parseInt.getIdentifier(), __string_parseInt);

        // __array_size
        ArrayList<Argument> array_size_paras = new ArrayList<>();
        array_size_paras.add(new Argument(null, STRING, 0, "arr"));
        Function __array_size = new Function("__array_size", I32, array_size_paras, true);
        FunctionMap.put(__array_size.getIdentifier(), __array_size);
    }

    public HashMap<String, Function> getFunctionMap() {
        return FunctionMap;
    }

    public HashMap<String, Value> getGlobalVarMap() {
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
        List<ParameterNode> paraList =  methodDecNode.getParaDecList();
        Argument pointerThis = new Argument(null, new PointerType(ClassMap.get(ClassName)), 0,
                paraList.get(0).getIdentifier());
        args.add(pointerThis);
        for (ParameterNode para : paraList) {
            Argument arg = new Argument(null, IRBuilder.ConvertTypeFromAST(para.getType()), id,
                    paraList.get(id).getIdentifier());
            args.add(arg);
            id += 1;
        }
        Function method = new Function(methodName, RetType, args, false);
        method.initialize();
        BasicBlock head = method.getHeadBlock();
        ArrayList<AllocaInst> AllocaList = new ArrayList<>();
        ArrayList<StoreInst> StoreList = new ArrayList<>();
        // argument shall have a default value

        for (Argument arg : method.getParameterList()) {
            AllocaInst ArgAddr = new AllocaInst(head, arg.type);
            AllocaList.add(ArgAddr);
            method.getVarSymTab().put(arg.getName(), ArgAddr);
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
        List<ParameterNode> paraList = FuncDecNode.getParaDecList();
        int id = 0;
        for (ParameterNode para : paraList) {
            Argument arg = new Argument(null, IRBuilder.ConvertTypeFromAST(para.getType()), id,
                    paraList.get(id).getIdentifier());
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
            function.getVarSymTab().put(arg.getName(), ArgAddr);
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

    public HashMap<String, GlobalVariable> getStringConstMap() {
        return StringConstMap;
    }

    public void setStringConstMap(HashMap<String, GlobalVariable> stringConstMap) {
        StringConstMap = stringConstMap;
    }
}
