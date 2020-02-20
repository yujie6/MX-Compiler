package IR;

/// LLVM Basic Block Representation
///
/// This represents a single basic block in LLVM. A basic block is simply a
/// container of instructions that execute sequentially. Basic blocks are Values
/// because they are referenced by instructions such as branches and switch
/// tables. The type of a BasicBlock is "Type::LabelTy" because the basic block
/// represents a label to which a branch can jump.
///
/// A well formed basic block is formed of a list of non-terminating
/// instructions followed by a single terminator instruction. Terminator
/// instructions may not occur in the middle of basic blocks, and must terminate
/// the blocks. The BasicBlock class allows malformed basic blocks to occur
/// because it may be useful in the intermediate stage of constructing or
/// modifying a program. However, the verifier will ensure that basic blocks are
/// "well formed".


import java.util.ArrayList;

public class BasicBlock extends Value {
    private Function Parent;
    private ArrayList<Instruction> InstList;
    private BasicBlock prev, next;

    public void setParent(Function parent) {
        Parent = parent;
    }

    public Function getParent() {
        return Parent;
    }
}
