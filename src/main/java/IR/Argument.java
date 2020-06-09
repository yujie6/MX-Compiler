package IR;

/// This class represents an incoming formal argument to a Function. A formal
/// argument, since it is ``formal'', does not contain an actual value but
/// instead represents the type, argument number, and attributes of an argument
/// for a specific function. When used in the body of said function, the
/// argument of course represents the value of the actual argument that the


import IR.Types.IRBaseType;
import Optim.MxOptimizer;

public class Argument extends Value {
    private Function Parent;
    private IRBaseType ArgType;
    private String name;
    private int ArgNo; // index of argument in the function
    public Function getParent() {
        return Parent;
    }

    public Argument(Function parent, IRBaseType argType, int argNo, String name) {
        super(ValueType.ARGUMENT);
        this.Parent = parent;
        this.ArgType = argType;
        this.type = argType;
        this.ArgNo = argNo;
        this.name = name;
    }

    public void replaceAllUsesWith(Value replaceValue) {
        for (User U : this.UserList) {
            replaceValue.UserList.add(U);
            boolean replaceDone = false;
            for (Use use : U.UseList) {
                if (use.getVal() == this) {
                    use.setVal(replaceValue);
                    replaceValue.UserList.add(U);
                    replaceDone = true;
                }
            }
            if (!replaceDone) {
                MxOptimizer.logger.severe("Replacing value fail!");
                System.exit(1);
            }
        }
        this.UserList.clear();
    }

    public int getArgNo() {
        return ArgNo;
    }

    public void setParent(Function parent) {
        Parent = parent;
    }

    public IRBaseType getArgType() {
        return ArgType;
    }

    public String getName() {
        return name;
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return null;
    }
}
