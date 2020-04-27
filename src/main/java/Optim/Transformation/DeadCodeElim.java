package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.*;
import IR.Use;
import IR.Value;
import Optim.MxOptimizer;
import Optim.Pass;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This file implements simple dead code elimination for ssa
 */
public class DeadCodeElim extends Pass {

    private Function function;
    private int eliminationNum;
    public DeadCodeElim(Function func) {
        this.function = func;
        this.eliminationNum = 0;
    }

    @Override
    public boolean optimize() {
        LinkedList<Instruction> workList = new LinkedList<>();
        for (BasicBlock BB : function.getBlockList()) {
            workList.addAll(BB.getInstList());
        }
        while (!workList.isEmpty()) {
            Instruction inst = workList.pop();
            if (inst instanceof ReturnInst || inst instanceof BranchInst
            || inst instanceof StoreInst) continue;
            if (inst instanceof CallInst) {
                continue;
            }
            if (inst.UserList.isEmpty()) {
                for (Use U : inst.UseList) {
                    Value t = U.getVal();
                    if (t instanceof Instruction && !workList.contains(t)) {
                        workList.add((Instruction) t);
                    }
                }
                inst.eraseFromParent();
                eliminationNum += 1;
            }
        }
        if (eliminationNum != 0) {
            MxOptimizer.logger.info("Dead code elimination runs on " + function.getIdentifier() +
                    ", with " + eliminationNum + " instructions eliminated");
            return true;
        }
        return false;
    }
}
