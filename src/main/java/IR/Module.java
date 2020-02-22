package IR;

import AST.FunctionDecNode;
import AST.MethodDecNode;
import AST.ParameterNode;
import BackEnd.IRBuilder;
import IR.Instructions.AllocaInst;
import IR.Instructions.StoreInst;
import IR.Types.*;

import java.util.ArrayList;
import java.util.HashMap;

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
    private ValueSymbolTable VarSymTab;
    private String TargetTriple;
    private String SourceFileName;
    private String ModuleID;

    public static final VoidType VOID = new VoidType();
    public static final IntegerType I1 = new IntegerType(IntegerType.BitWidth.i1);
    public static final IntegerType I8 = new IntegerType(IntegerType.BitWidth.i8);
    public static final IntegerType I16 = new IntegerType(IntegerType.BitWidth.i16);
    public static final IntegerType I32 = new IntegerType(IntegerType.BitWidth.i32);
    public static final PointerType STRING = new PointerType(I8);


    public Module(ValueSymbolTable varSymTab) {
        super(ValueType.MODULE);
        this.VarSymTab = varSymTab;
        GlobalVarMap = new HashMap<>();
        FunctionMap = new HashMap<>();
        ClassMap = new HashMap<>();

        PreProcess();
    }

    private void PreProcess() {
        // print(string str)
        ArrayList<Argument> print_paras = new ArrayList<>();
        print_paras.add(new Argument(null, STRING, 0));
        // TODO: set parent
        Function _print = new Function("print", VOID, print_paras);
        FunctionMap.put(_print.getIdentifier(), _print);
        // println(string str) -> end with '/n'
        ArrayList<Argument> println_paras = new ArrayList<>();
        println_paras.add(new Argument(null, STRING, 0));
        Function _println = new Function("println", VOID, println_paras);
        FunctionMap.put(_println.getIdentifier(), _println);
        // printlnInt(int n) -> end with '/n'
        ArrayList<Argument> printlnInt_paras = new ArrayList<>();
        printlnInt_paras.add(new Argument(null, I32, 0));
        Function _printlnInt = new Function("printlnInt", VOID, printlnInt_paras);
        FunctionMap.put(_printlnInt.getIdentifier(), _printlnInt);
        // toString(int i)
        ArrayList<Argument> toString_paras = new ArrayList<>();
        toString_paras.add(new Argument(null, I32, 0));
        Function _toString = new Function("toString", STRING, toString_paras);
        FunctionMap.put(_toString.getIdentifier(), _toString);
        // String add
        ArrayList<Argument> StringAdd_paras = new ArrayList<>();
        StringAdd_paras.add(new Argument(null, STRING, 0));
        StringAdd_paras.add(new Argument(null, STRING, 1));
        Function _StringAdd = new Function("toString", STRING, toString_paras);
        FunctionMap.put(_StringAdd.getIdentifier(), _StringAdd);
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

    public void defineFunction(Function func) {
        FunctionMap.put(func.getIdentifier(), func);
    }

    public void defineFunction(MethodDecNode methodDecNode, String ClassName) {
        // TODO Deal with method (first para is this)
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
        Function function = new Function(FuncName, RetType, args);
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

    public void defineClass() {

    }

    public void defineGlobalVar(GlobalVariable globalVariable) {
        GlobalVarMap.put(globalVariable.getIdentifier(), globalVariable);
    }

    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) {
        visitor.visit(this);
    }
}
