package Optim.FuncAnalysis;

import IR.BasicBlock;
import IR.Function;
import Optim.Pass;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This file is used for finding back edges and construct loop nested tree
 */
public class LoopAnalysis extends Pass {

    private Function function;
    private DomTreeBuilder dm;
    public Loop rootLoop;
    private HashSet<BackEdge> backEdges;
    private HashMap<BasicBlock, Loop> nestedLoops;
    private HashSet<BasicBlock> visited, loopHeaders;

    private class BackEdge {
        public BasicBlock head, tail;

        public BackEdge(BasicBlock head, BasicBlock tail) {
            this.head = head;
            this.tail = tail;
        }
    }

    public LoopAnalysis(Function function, DomTreeBuilder dm1) {
        this.function = function;
        this.dm = dm1;
        backEdges = new HashSet<>();
        visited = new HashSet<>();
        nestedLoops = new HashMap<>();
        loopHeaders = new HashSet<>();
    }

    @Override
    public boolean optimize() {
        DomNode root = dm.domTree.get(function.getHeadBlock());
        HashSet<BasicBlock> realLoopBlocks = new HashSet<>();
        // find all back edges
        buildBackEdge(root, null);
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
        buildLoopTree(rootLoop);
        return false;
    }

    private void buildLoopTree(Loop outerLoop) {
        for (BasicBlock BB : outerLoop.getLoopBlocks()) {
            if (BB == outerLoop.header) continue;
            if (loopHeaders.contains(BB)) {
                Loop innerLoop = nestedLoops.get(BB);
                innerLoop.setOuterLoop(outerLoop);
                outerLoop.addInnerLoop(innerLoop);
            }
        }
    }

    private void buildLoop(BasicBlock cur, BackEdge backEdge, Loop loop) {
        if (visited.contains(cur)) return;
        if (cur != backEdge.tail) loop.addBlock(cur);
        visited.add(cur);
        for (BasicBlock pred : cur.predecessors) {
            buildLoop(pred, backEdge, loop);
        }
    }

    private void buildBackEdge(DomNode node, DomNode father) {
        if (father != null) {
            if (node.block.successors.contains(father.block)) {
                backEdges.add(new BackEdge(node.block, father.block));
            }
        }
        for (DomNode child : node.children) {
            buildBackEdge(child, node);
        }
    }

    public HashMap<BasicBlock, Loop> getNestedLoops() {
        return nestedLoops;
    }
}
