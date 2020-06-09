package Optim.Transformation;

import IR.*;
import IR.Constants.BoolConst;
import IR.Constants.IntConst;
import IR.Constants.NullConst;
import IR.Constants.StringConst;
import IR.Instructions.*;
import Optim.FunctionPass;
import Optim.MxOptimizer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * This file implement conditional constant propagation
 */
public class CondConstPropag extends FunctionPass implements IRVisitor {

    private HashMap<BasicBlock, Boolean> Executed;
    private HashMap<Instruction, status> statusMap;
    private LinkedList<BasicBlock> blockWorkList;
    private LinkedList<Instruction> instWorkList;

    @Override
    public Object visit(BasicBlock node) {
        return null;
    }

    @Override
    public Object visit(Function node) {
        return null;
    }

    @Override
    public Object visit(AllocaInst allocaInst) {
        return null;
    }

    private void setStatus(Instruction inst, Value constVal) {
        if (!statusMap.containsKey(inst)) {
            statusMap.put(inst, new status(false)) ;
        }
        status s = statusMap.get(inst);
        s.unDefined = false;
        s.overDefined = false;
        s.constValue = ((IntConst) constVal).ConstValue;
        instWorkList.add(inst);
    }

    private void setStatus(Instruction inst, boolean overDefined) {
        if (!statusMap.containsKey(inst)) {
            statusMap.put(inst, new status(false)) ;
        }
        status s = statusMap.get(inst);
        s.overDefined = overDefined;
    }

    @Override
    public Object visit(BinOpInst binOpInst) {
        status s1 = getStatus(binOpInst.getLHS());
        status s2 = getStatus(binOpInst.getRHS());
        if (s1.isConst() && s2.isConst()) {
            Value replaceVal = ConstFold.getConstVal(binOpInst.Opcode, new IntConst(s1.constValue), new
                    IntConst(s2.constValue));
            setStatus(binOpInst, replaceVal);
        } else if (s1.overDefined || s2.overDefined) {
            setStatus(binOpInst, true);
        }
        return null;
    }

    @Override
    public Object visit(BitCastInst bitCastInst) {
        return null;
    }

    @Override
    public Object visit(BranchInst branchInst) {
        return null;
    }

    @Override
    public Object visit(CallInst callInst) {
        return null;
    }

    @Override
    public Object visit(CmpInst cmpInst) {
        status s1 = getStatus(cmpInst.getLHS());
        status s2 = getStatus(cmpInst.getRHS());
        if (s1.isConst() && s2.isConst()) {

        }
        return null;
    }

    @Override
    public Object visit(CopyInst copyInst) {
        return null;
    }

    @Override
    public Object visit(GetPtrInst getPtrInst) {
        return null;
    }

    @Override
    public Object visit(LoadInst loadInst) {
        return null;
    }

    @Override
    public Object visit(ReturnInst returnInst) {
        return null;
    }

    @Override
    public Object visit(PhiInst phiInst) {
        return null;
    }

    @Override
    public Object visit(SextInst sextInst) {
        return null;
    }

    @Override
    public Object visit(StoreInst storeInst) {
        return null;
    }

    private class status {
        public int constValue;
        public boolean unDefined;
        public boolean overDefined;

        public status(boolean overDefined) {
            if (overDefined) {
                this.unDefined = false;
                this.overDefined = true;
            } else {
                this.overDefined = false;
                this.unDefined = true;
            }
            this.constValue = 0;
        }

        public status(int val) {
            this.unDefined = false;
            this.overDefined = false;
            this.constValue = val;
        }

        public boolean isConst() {
            return !unDefined && !overDefined;
        }
    }

    public CondConstPropag(Function function) {
        super(function);
        this.statusMap = new HashMap<>();
        this.Executed = new HashMap<>();
        for (BasicBlock BB : function.getBlockList()) {
            Executed.put(BB, false);
        }
        this.blockWorkList = new LinkedList<>();
        this.instWorkList = new LinkedList<>();
    }

    private int elimNum = 0;

    @Override
    public boolean optimize() {
        this.elimNum = 0;
        this.statusMap.clear();
        this.Executed.clear();
        this.blockWorkList.clear();
        this.instWorkList.clear();

        mark(function.getHeadBlock());
        run();
        if (elimNum != 0) {
            MxOptimizer.logger.fine(String.format("SCCP running on \"%s\", with %d insts eliminated",
                    function.getIdentifier(), this.elimNum));
        }
        return elimNum != 0;
    }

    private void run() {

        while (!blockWorkList.isEmpty() || !instWorkList.isEmpty()) {
            while (!instWorkList.isEmpty()) {
                Instruction constInst = instWorkList.pop();
                for (User user : constInst.UserList) {
                    user.accept(this);
                }
            }

            while (!blockWorkList.isEmpty()) {
                BasicBlock BB = blockWorkList.pop();
                if (BB.successors.size() == 1) {
                    mark(BB.successors.iterator().next());
                }
                for (Instruction inst : BB.getInstList()) {
                    inst.accept(this);
                }
            }
        }

        for (BasicBlock BB : function.getBlockList()) {
            for (Instruction inst : List.copyOf(BB.getInstList() ) ) {
                status s = getStatus(inst);
                if (s.isConst()) {
                    this.elimNum += 1;
                    inst.replaceAllUsesWith(new IntConst(s.constValue));
                    inst.eraseFromParent();
                }
            }
        }
    }

    private status getStatus(Value val) {
        if (statusMap.containsKey(val)) {
            return statusMap.get(val);
        } else {
            if (val instanceof IntConst) {
                return new status(((IntConst) val).ConstValue);
            } else if (val instanceof BoolConst) {
                return new status(((BoolConst) val).constValue);
            } else if (val instanceof NullConst) {
                return new status(0);
            } else if (val instanceof Argument || val instanceof GlobalVariable) {
                return new status(true);
            } else {
                // val must be instruction ??
                status s = new status(false);
                this.statusMap.put(((Instruction) val), s);
                return s;
            }
        }
    }

    private void mark(BasicBlock BB) {
        if (!Executed.get(BB)) {
            Executed.replace(BB, true);
            blockWorkList.add(BB);
        }
    }
}
