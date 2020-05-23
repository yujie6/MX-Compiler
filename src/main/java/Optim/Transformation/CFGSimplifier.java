package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.Instruction;
import IR.Module;
import Optim.FunctionPass;
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


    private void merge(BasicBlock father, BasicBlock child) {
        father.RemoveTailInst();
        for (Instruction inst : child.getInstList()) {
            father.AddInstAtTail(inst);
        }
    }

    public Object visit(Function node) {
        boolean changed = false;
        for (BasicBlock BB : List.copyOf(node.getBlockList()) ) {
            if (BB.successors.size() == 1) {
                BasicBlock child = BB.successors.iterator().next();
                if (child.successors.size() == 1 && child.predecessors.contains(BB)) {
                    merge(BB, child);
                    changed = true;
                }
            }
        }
        return changed;
    }


    @Override
    public boolean optimize() {
        boolean changed = false;
        LinkedList<BasicBlock> workList = new LinkedList<>(function.getBlockList());
        while (!workList.isEmpty()) {
            BasicBlock BB = workList.pop();
            if (BB.predecessors.size() == 0 && BB != function.getHeadBlock()) {
                for (BasicBlock succ : BB.successors) {
                    succ.predecessors.remove(BB);
                    workList.add(succ);
                }
                function.removeBlock(BB);
            }
        }
        return changed;
    }
}
