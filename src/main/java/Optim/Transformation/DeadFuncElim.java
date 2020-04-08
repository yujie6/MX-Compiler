package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.CallInst;
import IR.Instructions.Instruction;
import IR.Module;
import Optim.Pass;

import java.util.HashMap;

public class DeadFuncElim extends Pass {

    private Module TopModule;
    private HashMap<String, Boolean> deadFunctions;

    public DeadFuncElim(Module topModule) {
        this.TopModule = topModule;
        deadFunctions = new HashMap<>();
    }

    @Override
    public boolean optimize() {
        for (String funcName : TopModule.getFunctionMap().keySet() ) {
            deadFunctions.put(funcName, true);
        }
        deadFunctions.replace("main", false);

        for (Function function : TopModule.getFunctionMap().values()) {
            if (!function.isExternal()) {
                visit(function);
            }
        }

        for (String funcName : deadFunctions.keySet()) {
            if (deadFunctions.get(funcName)) {
                TopModule.getFunctionMap().remove(funcName);
            }
        }
        return true;
    }

    private void visit(Function function) {
        for (BasicBlock BB : function.getBlockList()) {
            visit(BB);
        }
    }

    private void visit(BasicBlock BB) {
        for (Instruction inst : BB.getInstList()) {
            if (inst instanceof CallInst) {
                String funcName = ((CallInst) inst).getCalledFunction().getIdentifier();
                deadFunctions.replace(funcName, false);
            }
        }
    }
}
