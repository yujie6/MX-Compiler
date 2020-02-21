package IR.Types;

import IR.Value;

import java.util.ArrayList;


/**
 *  The function type can be thought of as a function signature. It consists of a return type and a list of
 *  formal parameter types. The return type of a function type is a void type or first class type
 *  — except for label and metadata types.
 */

public class FunctionType extends IRBaseType {

    private IRBaseType ReturnType;
    private ArrayList<IRBaseType> ArgumentTypeList;

    public FunctionType(IRBaseType returnType, ArrayList<IRBaseType> argumentTypeList) {
        this.BaseTypeName = TypeID.FunctionTyID;
        this.ReturnType = returnType;
        this.ArgumentTypeList = argumentTypeList;
    }

    public ArrayList<IRBaseType> getArgumentTypeList() {
        return ArgumentTypeList;
    }

    public IRBaseType getReturnType() {
        return ReturnType;
    }

    @Override
    public int getBytes() {
        return 0;
    }

    @Override
    public Value getDefaultValue() {
        return null;
    }
}
