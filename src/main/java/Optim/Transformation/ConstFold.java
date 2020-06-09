package Optim.Transformation;

import AST.ParameterNode;
import IR.BasicBlock;
import IR.Constants.BoolConst;
import IR.Constants.IntConst;
import IR.Function;
import IR.Instructions.BinOpInst;
import IR.Instructions.CmpInst;
import IR.Instructions.Instruction;
import IR.Value;
import Optim.FunctionPass;
import Optim.MxOptimizer;

import java.util.List;

public class ConstFold extends FunctionPass {

    public ConstFold(Function function) {
        super(function);
    }

    private int elimNum = 0;

    public static Value getConstVal(Instruction.InstType opcode, IntConst LHS, IntConst RHS) {
        switch (opcode) {
            case add:
                return new IntConst(LHS.ConstValue + RHS.ConstValue);
            case sub:
                return new IntConst(LHS.ConstValue - RHS.ConstValue);
            case shl:
                return new IntConst(LHS.ConstValue << RHS.ConstValue);
            case xor:
                return new IntConst(LHS.ConstValue ^ RHS.ConstValue);
            case mul:
                return new IntConst(LHS.ConstValue * RHS.ConstValue);
            case or:
                return new IntConst(LHS.ConstValue | RHS.ConstValue);
            case and:
                return new IntConst(LHS.ConstValue & RHS.ConstValue);
            case shr:
                return new IntConst(LHS.ConstValue >> RHS.ConstValue);
            default: {
                return null;
            }
        }
    }

    private void foldBinOpInst(BinOpInst binOpInst) {
        Instruction.InstType opcode = binOpInst.Opcode;
        Value LHS = binOpInst.getLHS();
        Value RHS = binOpInst.getRHS();
        if (LHS instanceof IntConst && RHS instanceof IntConst) {
            Value replaceVal = getConstVal(opcode, ((IntConst) LHS), ((IntConst) RHS));
            if (replaceVal == null) {

            }
            this.elimNum += 1;
            binOpInst.replaceAllUsesWith(replaceVal);
            binOpInst.eraseFromParent();
        } else if (LHS == RHS && opcode.equals(Instruction.InstType.sub)) {
            this.elimNum += 1;
            binOpInst.replaceAllUsesWith(new IntConst(0));
            binOpInst.eraseFromParent();
        }
    }

    private void foldCmpInst(CmpInst cmpInst) {
        Value LHS = cmpInst.getLHS();
        Value RHS = cmpInst.getRHS();
        if (LHS instanceof IntConst && RHS instanceof IntConst) {
            Value replaceVal = getCmpConstVal(cmpInst.SubOpcode, ((IntConst) LHS), ((IntConst) RHS));
            this.elimNum += 1;
            cmpInst.replaceAllUsesWith(replaceVal);
            cmpInst.eraseFromParent();
        } else if (cmpInst.SubOpcode.equals(CmpInst.CmpOperation.eq) && LHS == RHS) {
            this.elimNum += 1;
            cmpInst.replaceAllUsesWith(new BoolConst(true));
            cmpInst.eraseFromParent();
        }
    }

    private Value getCmpConstVal(CmpInst.CmpOperation subOpcode, IntConst lhs, IntConst rhs) {
        switch (subOpcode) {
            case eq:
                return new BoolConst(lhs.ConstValue == rhs.ConstValue);
            case ne:
                return new BoolConst(lhs.ConstValue != rhs.ConstValue);
            case uge:
            case sge:
                return new BoolConst(lhs.ConstValue >= rhs.ConstValue);
            case ugt:
            case sgt:
                return new BoolConst(lhs.ConstValue > rhs.ConstValue);
            case ule:
            case sle:
                return new BoolConst(lhs.ConstValue <= rhs.ConstValue);
            case ult:
            case slt:
                return new BoolConst(lhs.ConstValue < rhs.ConstValue);
            default:
                return null;
        }
    }

    private void foldInst(Instruction inst) {
        if (inst instanceof BinOpInst) {
            foldBinOpInst(((BinOpInst) inst));
        } else if (inst instanceof CmpInst) {
            foldCmpInst((CmpInst) inst);
        }
    }

    @Override
    public boolean optimize() {
        this.elimNum = 0;
        for (BasicBlock BB : function.getBlockList()) {
            for (Instruction inst : List.copyOf(BB.getInstList() ) ) {
                foldInst(inst);
            }
        }
        if (this.elimNum != 0) {
            MxOptimizer.logger.fine(String.format("ConstFolding running on \"%s\", with %d insts folded",
                    function.getIdentifier(), this.elimNum));
        }
        return this.elimNum != 0;
    }
}
