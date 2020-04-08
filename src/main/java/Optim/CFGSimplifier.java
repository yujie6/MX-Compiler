package Optim;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.Instruction;
import IR.Module;

public class CFGSimplifier extends Pass {


    private Module TopModule;

    public CFGSimplifier(Module topModule) {
        this.TopModule = topModule;
    }


    private void merge(BasicBlock father, BasicBlock child) {
        father.RemoveTailInst();
        for (Instruction inst : child.getInstList()) {
            father.AddInstAtTail(inst);
        }
    }

    public Object visit(Function node) {
        boolean changed = false;
        for (BasicBlock curBasicBlock = node.getHeadBlock(); curBasicBlock != node.getTailBlock();
        curBasicBlock = curBasicBlock.getNext() ) {
            if (curBasicBlock.successors.size() == 1) {
                BasicBlock child = curBasicBlock.successors.iterator().next();
                if (child.successors.size() == 1 && child.predecessors.contains(curBasicBlock)) {
                    merge(curBasicBlock, child);
                    changed = true;
                }
            }
        }
        return changed;
    }

    @Override
    boolean optimize() {
        boolean changed = false;
        for (Function func : TopModule.getFunctionMap().values()) {
            if (!func.isExternal())
                changed |= (boolean) visit(func);
        }
        return changed;
    }
}
