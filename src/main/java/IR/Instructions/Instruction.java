package IR.Instructions;

import IR.BasicBlock;
import IR.User;

public abstract class Instruction extends User {
    protected BasicBlock Parent;
    protected InstType instType;
    protected Instruction prev, next;

    public enum InstType {
        Alloca, BinOp, Branch, Call, Equals, Load, Store, Phi, Return, Cast,
        Cmp, GetPtr, UnaOp
    }

    public Instruction(BasicBlock parent, InstType instType) {
        this.Parent = parent;
        this.VTy = ValueType.INSTRUCTION;
        this.instType = instType;
    }

    public BasicBlock getParent() {
        return Parent;
    }

    public boolean isTerminalInst() {
        return instType == InstType.Return || instType == InstType.Branch;
    }

    public void setNext(Instruction next) {
        this.next = next;
    }

    public void setPrev(Instruction prev) {
        this.prev = prev;
    }

    public Instruction getNext() {
        return next;
    }

    public Instruction getPrev() {
        return prev;
    }
}
