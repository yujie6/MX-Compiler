package IR;

import IR.Types.IRBaseType;

import java.util.ArrayList;
/**
 * This is a very important LLVM class. It is the base class of all values
 * computed by a program that may be used as operands to other values. Value is
 * the super class of other important classes such as Instruction and Function.
 * All Values have a Type. Type is not a subclass of Value. Some values can
 * have a name and they belong to some Module.  Setting the name on the Value
 * automatically updates the module's symbol table.
 *
 * A particular Value may be used many times in the LLVM representation for a program.
 * For example, an incoming argument to a function (represented with an instance of the Argument class)
 * is “used” by every instruction in the function that references the argument. To keep track of this
 * relationship, the Value class keeps a list of all of the Users that is using it (the User class is a
 * base class for all nodes in the LLVM graph that can refer to Values). This use list is how LLVM
 * represents def-use information in the program.
 */

public abstract class Value extends IRBaseNode {
    public ValueType VTy;

    protected IRBaseType type;

    public ArrayList<User> UserList;

    public enum ValueType {
        INSTRUCTION,
        ARGUMENT,
        FUNCTION,
        CONSTANT,
        GLOBAL_VAR,
        BASIC_BLOCK,
        MODULE
    }

    public Value(ValueType vTy) {
        this.UserList = new ArrayList<>();
        this.VTy = vTy;
    }

    public IRBaseType getType() {
        return type;
    }

    public void setType(IRBaseType type) {
        this.type = type;
    }

    public ValueType getVTy() {
        return VTy;
    }
}
