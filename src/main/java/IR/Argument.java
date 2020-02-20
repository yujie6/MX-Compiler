package IR;

/// This class represents an incoming formal argument to a Function. A formal
/// argument, since it is ``formal'', does not contain an actual value but
/// instead represents the type, argument number, and attributes of an argument
/// for a specific function. When used in the body of said function, the
/// argument of course represents the value of the actual argument that the


public class Argument extends Value {
    private Function Parent;
    private int ArgNo; // index of argument in the function
    public Function getParent() {
        return Parent;
    }

    public Argument(Function parent, int argNo) {
        this.Parent = parent;

    }
}
