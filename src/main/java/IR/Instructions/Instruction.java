package IR.Instructions;

import IR.BasicBlock;
import IR.User;

public abstract class Instruction extends User {
    protected BasicBlock Parent;
    protected InstType Opcode;
    protected Instruction prev, next;

    public enum InstType {
        Alloca, Add, Minus, Branch, Call, Equals, Load, Store, Phi, Return
    }

    public Instruction(BasicBlock parent) {
        this.Parent = parent;
        this.VTy = ValueType.INSTRUCTION;
    }

    public BasicBlock getParent() {
        return Parent;
    }

    public boolean isTerminalInst() {
        return Opcode == InstType.Return || Opcode == InstType.Branch;
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
