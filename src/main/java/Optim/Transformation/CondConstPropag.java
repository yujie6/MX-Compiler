package Optim.Transformation;

import IR.BasicBlock;
import IR.Constants.IntConst;
import IR.Function;
import IR.Instructions.*;
import IR.Value;
import Optim.FunctionPass;
import Optim.Pass;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This file implement conditional constant propagation
 */
public class CondConstPropag extends FunctionPass {

    private HashMap<BasicBlock, Boolean> Executed;
    private HashMap<Instruction, status> statusMap;
    private LinkedList<BasicBlock> blockWorkList;
    private LinkedList<Instruction> instWorkList;

    private class status {
        public int constValue;
        public boolean unDefined;
        public boolean overDefined;

        public status() {
            this.unDefined = true;
            this.overDefined = false;
            this.constValue = 0;
        }

        public boolean isConst() {
            return !unDefined && !overDefined;
        }
    }

    public CondConstPropag(Function function) {
        super(function);
        this.statusMap = new HashMap<>();
        for (BasicBlock BB : function.getBlockList()) {
            Executed.put(BB, false);
        }
        this.blockWorkList = new LinkedList<>();
        this.instWorkList = new LinkedList<>();
    }

    @Override
    public boolean optimize() {
        mark();
        return false;
    }

    private void mark() {
        mark(function.getHeadBlock());
        while (!blockWorkList.isEmpty() || !instWorkList.isEmpty()) {
            if (!blockWorkList.isEmpty()) {
                BasicBlock BB = blockWorkList.pop();
                if (BB.successors.size() == 1) {
                    mark(BB.successors.iterator().next());
                }
                for (Instruction inst : BB.getInstList()) {
                    updateStatus(inst);
                }
            } else {
                Instruction inst = instWorkList.pop();
                updateStatus(inst);
            }
        }
    }

    private void updateStatus(Instruction inst) {
        status status_t = statusMap.get(inst);
        boolean oldUnDefined = status_t.unDefined;
        boolean oldOverDefined = status_t.overDefined;
        if (inst instanceof BinOpInst) {
            Value LHS = ((BinOpInst) inst).getLHS();
            Value RHS = ((BinOpInst) inst).getRHS();
            if (LHS instanceof IntConst && RHS instanceof IntConst) {
                status_t.unDefined = false;

            } else {
                if (LHS instanceof Instruction) {
                    status status_l = statusMap.get(LHS);
                    if (status_l.overDefined) {
                        status_t.overDefined = true;
                        status_t.unDefined = false;
                    }
                } else if (RHS instanceof Instruction) {
                    status status_r = statusMap.get(RHS);
                    if (status_r.overDefined) {
                        status_t.overDefined = true;
                        status_t.unDefined = false;
                    }
                }
            }
        } else if (inst instanceof LoadInst || inst instanceof CallInst) {
            status_t.overDefined = true;
            status_t.unDefined = false;
        } else if (inst instanceof BranchInst) {
            BranchInst br = ((BranchInst) inst);
            if (br.isHasElse()) {
                Value cond = br.getCondition();
                if (cond instanceof IntConst) {
                    if (((IntConst) cond).ConstValue == 1) {
                        Executed.replace(br.getThenBlock(), true);
                    } else {
                        Executed.replace(br.getElseBlock(), true);
                    }
                } else if (cond instanceof Instruction) {
                    status status_cond = statusMap.get(cond);
                    if (status_cond.overDefined) {
                        Executed.replace(br.getThenBlock(), true);
                        Executed.replace(br.getElseBlock(), true);
                    } else if (status_cond.isConst()) {
                        if (status_cond.constValue == 1) {
                            Executed.replace(br.getThenBlock(), true);
                        } else {
                            Executed.replace(br.getElseBlock(), true);
                        }
                    }
                }
            }
        } else if (inst instanceof PhiInst) {
            int s = 0;
            int executable = 0;
            int constVal = -1;
            PhiInst phi = ((PhiInst) inst);
            for (int i = 0; i < phi.getBranchNum(); i++) {
                BasicBlock BB = phi.getBlock(i);
                Value val = phi.getValue(i);
                if (Executed.get(BB)) {
                    executable += 1;
                    if (val instanceof IntConst) {
                        s ++ ;
                        constVal = ((IntConst) val).ConstValue;
                    } else if (val instanceof Instruction) {
                        boolean isConst = statusMap.get(val).isConst();
                        s += isConst ? 1 : 0;
                        constVal = isConst ? statusMap.get(val).constValue : constVal;
                    }
                }
                if (s >= 2) {
                    status_t.overDefined = true;
                    status_t.unDefined = false;
                    break;
                }
            }

            if (executable == 1 && s == 1) {
                status_t.unDefined = false;
                status_t.constValue = constVal;
            }

        }

        if (oldOverDefined != status_t.overDefined || oldUnDefined != status_t.unDefined) {
            instWorkList.add(inst);
        }

    }

    private void mark(BasicBlock BB) {
        if (!Executed.get(BB)) {
            Executed.replace(BB, true);
            blockWorkList.add(BB);
        }
    }
}
