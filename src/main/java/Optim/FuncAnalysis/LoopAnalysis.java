package Optim.FuncAnalysis;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.BranchInst;
import IR.Instructions.Instruction;
import IR.Instructions.PhiInst;
import Optim.MxOptimizer;
import Optim.Pass;
import org.antlr.v4.tool.ast.BlockAST;

import java.util.*;

/**
 * This file is used for finding back edges and construct loop nested tree
 */
public class LoopAnalysis extends Pass {

    private Function function;
    private DomTreeBuilder dm;
    public Loop rootLoop;
    private HashSet<BackEdge> backEdges;
    private HashMap<BasicBlock, Loop> nestedLoops, loopMap;
    private HashSet<BasicBlock> visited, loopHeaders;

    private class BackEdge {
        public BasicBlock head, tail;

        public BackEdge(BasicBlock head, BasicBlock tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BackEdge backEdge = (BackEdge) o;
            return head == backEdge.head &&
                    tail == backEdge.tail;
        }

        @Override
        public int hashCode() {
            return head.toString().hashCode() ^ tail.toString().hashCode();
        }
    }

    public LoopAnalysis(Function function, DomTreeBuilder dm1) {
        this.function = function;
        function.setLA(this);
        this.dm = dm1;
        backEdges = new HashSet<>();
        visited = new HashSet<>();
        nestedLoops = new HashMap<>();
        loopMap = new HashMap<>();
        loopHeaders = new HashSet<>();
    }

    @Override
    public boolean optimize() {
        backEdges.clear();
        visited.clear();
        nestedLoops.clear();
        loopMap.clear();
        loopHeaders.clear();
        DomNode root = dm.domTree.get(function.getHeadBlock());
        HashSet<BasicBlock> realLoopBlocks = new HashSet<>();
        // find all back edges
        buildBackEdge(root);
        // find all natural loops and merge them as nested loop
        for (BackEdge backEdge : backEdges) {
            Loop naturalLoop = new Loop(backEdge.tail);
            naturalLoop.addBlock(backEdge.tail);
            loopHeaders.add(backEdge.tail);
            realLoopBlocks.addAll(visited);
            visited.clear();
            buildLoop(backEdge.head, backEdge, naturalLoop);
            if (!nestedLoops.containsKey(backEdge.tail)) {
                nestedLoops.put(backEdge.tail, naturalLoop);
            } else {
                Loop anotherLoop = nestedLoops.get(backEdge.tail);
                naturalLoop.merge(anotherLoop);
                nestedLoops.replace(backEdge.tail, naturalLoop);
            }
        }
        // build the loop-nest tree
        rootLoop = new Loop(function.getHeadBlock());
        for (BasicBlock BB : function.getBlockList()) {
            if (!realLoopBlocks.contains(BB)) {
                rootLoop.addBlock(BB);
            }
        }
        buildLoopTree(rootLoop); // rootLoop may not be a loop
        this.preheaderNum = 0;
        addPreheader(rootLoop);
        buildLoopDepth(rootLoop, 0);
        if (preheaderNum != 0)
            MxOptimizer.logger.fine(String.format("Loop analysis on function \"%s\" done, with %d preheaders added."
                , function.getIdentifier(), preheaderNum));
        return !loopMap.isEmpty();
    }

    private void buildLoopDepth(Loop outerLoop, int depth) {
        for (BasicBlock BB : outerLoop.getLoopBlocks()) {
            BB.loopDepth = depth;
        }
        for (Loop insideLoop : outerLoop.insideLoops) {
            if (insideLoop.outerLoop != outerLoop) continue;
            buildLoopDepth(insideLoop, depth + 1);
        }
    }

    private void buildLoopTree(Loop outerLoop) {
        for (BasicBlock BB : outerLoop.getLoopBlocks()) {
            if (BB == outerLoop.header) continue;
            if (loopHeaders.contains(BB)) {
                Loop innerLoop = nestedLoops.get(BB);
                if (innerLoop.outerLoop == null || innerLoop.outerLoop == rootLoop) {
                    // each inner loop is visited once
                    innerLoop.setOuterLoop(outerLoop);
                    outerLoop.addInnerLoop(innerLoop);
                    buildLoopTree(innerLoop);
                }
            }
        }
    }

    private void buildLoop(BasicBlock cur, BackEdge backEdge, Loop loop) {
        if (visited.contains(cur) || cur == backEdge.tail) return;
        if (dm.dominates(backEdge.tail, cur)) loop.addBlock(cur);
        visited.add(cur);
        for (BasicBlock pred : cur.predecessors) {
            buildLoop(pred, backEdge, loop);
        }
    }

    private void buildBackEdge(DomNode node) {
        BasicBlock BB = node.block;
        for (BasicBlock pred : BB.predecessors) {
            if (node.dominates(dm.domTree.get(pred))) {
                backEdges.add(new BackEdge(pred, BB));
            }
        }
        for (DomNode child : node.children) {
            buildBackEdge(child);
        }
    }


    private int preheaderNum;

    public void addPreheader(Loop outerLoop) {
        for (Loop insideLoop : outerLoop.insideLoops) {
            if (insideLoop.outerLoop == outerLoop) {
                addPreheader(insideLoop);
                outerLoop.getLoopBlocks().addAll(insideLoop.getLoopBlocks());
            }
        }
        if (outerLoop != rootLoop) {
            BasicBlock preheader = new BasicBlock(function, "loop_preheader");
            outerLoop.preHeader = preheader;
            BasicBlock header = outerLoop.header;
            preheader.AddInstAtTail(new BranchInst(preheader, null, header, null));


            for (BasicBlock predecessor : Set.copyOf(header.predecessors)) {
                if (predecessor == preheader) continue;
                BranchInst br = (BranchInst) predecessor.getTailInst();
                br.replaceSuccBlock(header, preheader);
            }
            for (Instruction inst : List.copyOf(header.getInstList()) ) {
                if (inst instanceof PhiInst) {
                    // inst.eraseFromParent(); !!!
                    inst.getParent().getInstList().remove(this);
                    preheader.AddInstAtTop(inst);
                } else break;
            }
            this.preheaderNum += 1;
            function.AddBlockBefore(header, preheader);
            outerLoop.addBlock(preheader);
            this.loopMap.put(preheader, outerLoop);
        }
    }
}
