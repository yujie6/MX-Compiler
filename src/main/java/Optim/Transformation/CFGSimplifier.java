package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.Instruction;
import IR.Module;
import Optim.FunctionPass;
import Optim.MxOptimizer;
import Optim.Pass;

import java.util.LinkedList;
import java.util.List;

/**
 * The SimplifyCFG (SCFG) plugin tries to find or create basic blocks that can be entirely deleted from a
 * function.
 */
public class CFGSimplifier extends FunctionPass {


    public CFGSimplifier(Function function1) {
        super(function1);
    }

    private int elimNum = 0;

    public boolean visit(Function node) {
        this.elimNum = 0;
        for (BasicBlock BB : List.copyOf(node.getBlockList())) {
            if (BB.successors.size() == 1) {
                BasicBlock child = BB.successors.iterator().next();
                // consider the replacement of PhiInst
                if (child.predecessors.size() == 1 && child.predecessors.contains(BB)) {
                    BB.successors.remove(child);
                    BB.mergeWith(child);
                    elimNum += 1;
                }
                if (BB.predecessors.size() == 0 && BB != function.getHeadBlock()) {
                    function.getBlockList().remove(BB);
                    BB.successors.forEach(succ -> {
                        succ.predecessors.remove(BB); // remove phi?
                    });
                }
            }
        }
        if (elimNum != 0) {
            MxOptimizer.logger.fine("CFG simplify " + elimNum + " BBs for function: " + function.getIdentifier());
        }
        return elimNum != 0;
    }


    @Override
    public boolean optimize() {
        return visit(function);
    }
}
