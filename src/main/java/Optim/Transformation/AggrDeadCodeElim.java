package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.*;
import IR.Use;
import Optim.FuncAnalysis.CDGBuilder;
import Optim.Pass;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;


/**
 * This file implements aggressive dead code elimination for ssa
 * mark-sweep in particular, which make use of dependence graph
 */
public class AggrDeadCodeElim extends Pass {
    private Function function;
    private CDGBuilder cdgBuilder;
    private LinkedList<Instruction> workList;
    private ArrayList<Instruction> markedInsts;
    private Set<BasicBlock> markedBlocks;
    public AggrDeadCodeElim(Function func, CDGBuilder cdg) {
        this.function = func;
        this.cdgBuilder = cdg;
        markedBlocks = new LinkedHashSet<>();
        workList = new LinkedList<>();
        markedInsts = new ArrayList<>();
    }

    @Override
    public boolean optimize() {
        init();
        mark();
        return sweep();
    }


    private void init() {
        for (BasicBlock BB : function.getBlockList()) {
            for (Instruction inst : BB.getInstList()) {
                if (isCritical(inst)) {
                    mark(inst);
                    mark(BB);
                }
            }
        }
    }

    private void mark() {
        while (!workList.isEmpty()) {
            Instruction inst = workList.pop();
            for (Use U : inst.UseList) {
                if (U.getVal() instanceof Instruction) {
                    mark((Instruction) U.getVal());
                    markedBlocks.add(inst.getParent());

                    BasicBlock cur = ((Instruction) U.getVal()).getParent();
                    for (BasicBlock BB : cdgBuilder.postDomFrontier.get(cur)) {
                        mark(BB);
                    }

                }
            }
        }
    }

    public void mark(BasicBlock BB) {
        markedBlocks.add(BB);
        Instruction terminalInst = BB.getTailInst();
        mark(terminalInst);
    }

    private void mark(Instruction inst) {
        if (!markedInsts.contains(inst)) {
            markedInsts.add(inst);
            workList.add(inst);
        }
    }

    private boolean sweep() {
        boolean changed = false;
        for (BasicBlock BB : function.getBlockList()) {
            for (Instruction inst : BB.getInstList()) {
                // TODO read book and re-implement
                if (inst instanceof BranchInst) {
                    if (((BranchInst) inst).isHasElse()) {
                        // remove one branch & delete the other block
                    } else inst.eraseFromParent();
                    changed = true;
                } else {
                    inst.eraseFromParent();
                    changed = true;
                }
            }
        }
        // we can even delete head block
        return changed;
    }

    private boolean isCritical(Instruction inst ) {
        if (inst instanceof StoreInst || inst instanceof ReturnInst) {
            return true;
        } else if (inst instanceof CallInst) {
            Function calledFunc = ((CallInst) inst).getCallee();
            return hasSideEffect(calledFunc);
        }
        return false;
    }

    private boolean hasSideEffect(Function function) {
        // store on value & input, output ...
        return true;
    }

}
