package IR.Instructions;

import IR.*;
import IR.Constants.Constant;
import Optim.MxOptimizer;

import java.awt.image.BandedSampleModel;

public abstract class Instruction extends User {
    static protected int regNum = 1;
    protected BasicBlock Parent;
    public InstType Opcode;

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
        this.Parent.getInstList().remove(this);
        for (Use U : UseList) {
            if (U.getVal() instanceof Instruction) {
                Instruction inst = (Instruction) U.getVal();
                inst.UserList.remove(this);
            }
        }
        this.Parent = null;
    }

    public void addInstBefore(Instruction other) {
        int index = Parent.getInstList().indexOf(this);
        Parent.getInstList().add(index, other);
        if (index == 0) {
            Parent.setHeadInst(other);
        }
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

    public abstract void copyTo(BasicBlock other, IRMap irMap);

    public boolean isCommutative() {
        return Opcode.equals(InstType.add) || Opcode.equals(InstType.xor) ||
                Opcode.equals(InstType.and) || Opcode.equals(InstType.mul) ||
                Opcode.equals(InstType.or);
    }

    public void setParent(BasicBlock parent) {
        Parent = parent;
    }

    public boolean isTerminalInst() {
        return Opcode == InstType.ret || Opcode == InstType.br;
    }

    public void setRegisterID(String registerID) {
        RegisterID = registerID;
    }

    public String getRegisterID() {
        return RegisterID;
    }

    public abstract String toString();
}
