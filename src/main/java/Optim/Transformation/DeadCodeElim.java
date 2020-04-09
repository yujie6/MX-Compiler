package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.Instruction;
import IR.Use;
import IR.Value;
import Optim.Pass;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This file implements simple dead code elimination for ssa
 */
public class DeadCodeElim extends Pass {

    private Function function;
    public DeadCodeElim(Function func) {
        this.function = func;
    }

    @Override
    public boolean optimize() {
        boolean changed = false;
        LinkedList<Instruction> workList = new LinkedList<>();
        for (BasicBlock BB : function.getBlockList()) {
            workList.addAll(BB.getInstList());
        }
        while (!workList.isEmpty()) {
            Instruction inst = workList.pop();
            if (inst.UserList.isEmpty()) {
                for (Use U : inst.UseList) {
                    Value t = U.getVal();
                    if (t instanceof Instruction && !workList.contains(t)) {
                        workList.add((Instruction) t);
                    }
                }
                inst.eraseFromParent();
                changed = true;
            }
        }

        return changed;
    }
}
