package IR.Constants;

import IR.IRVisitor;
import IR.Types.IRBaseType;
import IR.User;

/**
 * Constant represents a base class for different types of constants. It is subclassed by ConstantInt
 * , ConstantArray, etc. for representing the various types of Constants. GlobalValue is also a
 * subclass, which represents the address of a global variable or function.
 */

public abstract class Constant extends User {

    public Constant(IRBaseType type) {
        super(ValueType.CONSTANT);
        this.type = type;
    }

    public abstract Object getValue();

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return null;
    }
}
