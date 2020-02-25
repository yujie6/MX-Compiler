package IR.Types;

import IR.Argument;
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
    private String Identifiler;

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

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(ReturnType.toString() + " @" + this.Identifiler + "(");
        if (ArgumentTypeList.size() != 0) {
            ans.append(ArgumentTypeList.get(0).toString());
            for (int i = 1; i < ArgumentTypeList.size(); i++) {
                ans.append(", ").append(ArgumentTypeList.get(i).toString());
            }
        }
        ans.append(")");
        return ans.toString();
    }

    public void setIdentifiler(String identifiler) {
        Identifiler = identifiler;
    }
}
