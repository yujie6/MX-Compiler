package IR.Instructions;

import IR.BasicBlock;
import IR.User;

public abstract class Instruction extends User {
    static protected int regNum = 1;
    protected BasicBlock Parent;
    protected InstType Opcode;
    protected Instruction prev, next;
    protected String RegisterID;

    public enum InstType {
        alloca, br, call, load, store, phi, ret, bitcast,
        icmp, getelementptr,
        add, sub, mul, div, shl, shr, srem, and, or, xor
    }

    public Instruction(BasicBlock parent, InstType instType) {
        super(ValueType.INSTRUCTION);
        this.Parent = parent;
        this.Opcode = instType;

        if (   !(instType.equals(InstType.br) || instType.equals(InstType.store))   ) {
            this.RegisterID = "%" + regNum;
            regNum += 1;
        }
    }

    public String getOpcode() {
        return Opcode.toString();
    }

    public BasicBlock getParent() {
        return Parent;
    }

    public boolean isTerminalInst() {
        return Opcode == InstType.ret || Opcode == InstType.br;
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

    public abstract String toString();
}
