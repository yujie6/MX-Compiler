package IR;

/// This class represents an incoming formal argument to a Function. A formal
/// argument, since it is ``formal'', does not contain an actual value but
/// instead represents the type, argument number, and attributes of an argument
/// for a specific function. When used in the body of said function, the
/// argument of course represents the value of the actual argument that the


import IR.Types.IRBaseType;

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

    public int getArgNo() {
        return ArgNo;
    }

    public void setParent(Function parent) {
        Parent = parent;
    }

    public IRBaseType getArgType() {
        return ArgType;
    }

    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) {

    }

    public String getName() {
        return name;
    }
}
