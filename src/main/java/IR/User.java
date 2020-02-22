package IR;

/*
 * The User class is the common base class of all LLVM nodes that may refer to Values. It exposes a list of “Operands”
 * that are all of the Values that the User is referring to. The User class itself is a subclass of Value.
 *
 * The operands of a User point directly to the LLVM Value that it refers to. Because LLVM uses SSA form, there
 * can only be one definition referred to, allowing this direct connection. This connection provides the
 * use-def information in LLVM.
 */

import java.util.ArrayList;

public class User extends Value {

    public ArrayList<Use> UseList;

    public ArrayList<Use> getUseList() {
        return UseList;
    }


    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) {

    }
}
