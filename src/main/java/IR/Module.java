package IR;

import AST.FunctionDecNode;
import AST.MethodDecNode;
import IR.Types.IntegerType;
import IR.Types.PointerType;
import IR.Types.StructureType;
import IR.Types.VoidType;

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

    }

    public void defineFunction(FunctionDecNode methodDecNode) {

    }

    public void defineClass() {

    }

    public void defineGlobalVar() {

    }

    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) {
        visitor.visit(this);
    }
}
