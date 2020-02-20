package IR;

import java.util.ArrayList;

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
    private ArrayList<GlobalVariable> GlobalVarList;
    private ArrayList<Function> FuncList;
    private ValueSymbolTable VarSymTab;
    private String TargetTriple;
    private String SourceFileName;
    private String ModuleID;

    public Module(ValueSymbolTable varSymTab) {
        this.VarSymTab = varSymTab;
    }

    public void addFunction(Function func) {
        FuncList.add(func);
    }

    public ArrayList<Function> getFuncList() {
        return FuncList;
    }

    public ValueSymbolTable getVarSymTab() {
        return VarSymTab;
    }

    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) {
        visitor.visit(this);
    }
}
