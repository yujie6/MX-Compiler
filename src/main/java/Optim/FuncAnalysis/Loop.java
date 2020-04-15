package Optim.FuncAnalysis;

import IR.BasicBlock;
import IR.Instructions.BranchInst;
import IR.Instructions.Instruction;

import java.util.HashSet;

/**
 * helper class for loop analysis
 */
public class Loop {
    public BasicBlock header, preHeader;
    private HashSet<BasicBlock> loopBlocks;
    public HashSet<Loop> insideLoops;
    public Loop outerLoop;

    public Loop(BasicBlock header) {
        this.header = header;
        this.insideLoops = new HashSet<>();
        this.loopBlocks = new HashSet<>();
        this.loopBlocks.add(header);
    }

    public void addPreHeader() {
        if (preHeader != null) return;
        preHeader = new BasicBlock(header.getParent(), "loop preHeader");
        BranchInst br = new BranchInst(preHeader, null, header, null);
        preHeader.AddInstAtTail(br);
        preHeader.addSuccessor(header);
        for (BasicBlock pred : header.predecessors) {
            header.predecessors.remove(pred);
            pred.successors.remove(header);
            pred.successors.add(preHeader);
            Instruction lastInst = pred.getTailInst();
            // TODO redirecting pred to pre-header & change phiNodes!!!
        }
    }

    public void addBlock(BasicBlock BB) {
        loopBlocks.add(BB);
    }

    public void addInnerLoop(Loop insideLoop) {
        insideLoops.add(insideLoop);
    }

    public void setOuterLoop(Loop outerLoop) {
        this.outerLoop = outerLoop;
    }

    public HashSet<BasicBlock> getLoopBlocks() {
        return loopBlocks;
    }

    public void merge(Loop other) {
        if (header != other.header) return;
        loopBlocks.addAll(other.loopBlocks);
    }
}
