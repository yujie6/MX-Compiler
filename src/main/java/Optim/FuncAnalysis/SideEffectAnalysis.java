package Optim.FuncAnalysis;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.CallInst;
import IR.Instructions.Instruction;
import IR.Instructions.StoreInst;
import Optim.Pass;

/**
 * Check if a function has side effect, that is, store
 * to global variable or other memory access
 */
public class SideEffectAnalysis extends Pass {

    private Function function;

    public SideEffectAnalysis(Function function) {
        this.function = function;
    }

    @Override
    public boolean optimize() {

        for (BasicBlock BB : function.getBlockList()) {
            for (Instruction inst : BB.getInstList()) {
                if (inst instanceof StoreInst) {

                } else if (inst instanceof CallInst) {

                }
            }
        }
        return false;
    }
}
