package IR.Instructions;

import IR.*;
import IR.Constants.Constant;
import Optim.MxOptimizer;

public abstract class Instruction extends User {
    static protected int regNum = 1;
    protected BasicBlock Parent;
    public InstType Opcode;
    protected Instruction prev, next;
    protected String RegisterID;

    public enum InstType {
        alloca, br, call, load, store, phi, ret, bitcast, sext,
        icmp, getelementptr,
        add, sub, mul, sdiv, shl, shr, srem, and, or, xor,
        copy
    }

    public Instruction(BasicBlock parent, InstType instType) {
        super(ValueType.INSTRUCTION);
        this.Parent = parent;
        this.Opcode = instType;

        if (!(instType.equals(InstType.br) || instType.equals(InstType.store) ||
                instType.equals(InstType.copy))) {
            this.RegisterID = "%" + regNum;
            regNum += 1;
        }
    }

    public static String getRightValueLabel(Value rightValue) {
        if (rightValue instanceof Constant) {
            return rightValue.toString();
        } else if (rightValue instanceof GlobalVariable) {
            return "@" + ((GlobalVariable) rightValue).getIdentifier();
        } else if (rightValue instanceof Argument) {
            return "%" + ((Argument) rightValue).getArgNo();
        } else {
            return ((Instruction) rightValue).getRegisterID();
        }
    }

    public String getOpcode() {
        return Opcode.toString();
    }

    public BasicBlock getParent() {
        return Parent;
    }

    public void eraseFromParent() {
        if (this.prev != null) this.prev.next = this.next;
        if (this.next != null) this.next.prev = this.prev;
        this.Parent.getInstList().remove(this);
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

    public boolean isCommutative() {
        return Opcode.equals(InstType.add) || Opcode.equals(InstType.xor) ||
                Opcode.equals(InstType.and) || Opcode.equals(InstType.mul) ||
                Opcode.equals(InstType.or);
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

    public void setRegisterID(String registerID) {
        RegisterID = registerID;
    }

    public String getRegisterID() {
        return RegisterID;
    }

    public abstract String toString();
}
