package BackEnd;

import IR.BasicBlock;
import IR.Function;
import IR.Module;
import Optim.ModulePass;
import Tools.MXLogger;

import java.util.LinkedList;

/**
 * Prior to SSADestruction, we need to prevent the case
 * that a -> b, while a.succ.size() > 1 && b.pred.size() > 1
 * by simply adding a new cfg node z, a -> z -> b
 */
public class EdgeSplitter extends ModulePass {

    private LinkedList<CriticalEdge> criticalEdgeList;
    private MXLogger logger;
    private class CriticalEdge {
        public BasicBlock A, B;

        public CriticalEdge(BasicBlock a, BasicBlock b) {
            A = a;
            B = b;
        }

        public Function getParent() {
            return A.getParent();
        }
    }

    public EdgeSplitter(Module TopModule, MXLogger logger) {
        super(TopModule);
        this.criticalEdgeList = new LinkedList<>();
        this.logger = logger;
    }

    private void splitEdge(CriticalEdge edge) {
        BasicBlock splitter = new BasicBlock(edge.getParent(), "edge splitter");

    }

    private boolean splitEdge(Function function) {
        boolean changed = false;
        for (BasicBlock BB : function.getBlockList()) {
            for (BasicBlock succ : BB.successors) {
                if (succ.predecessors.size() > 1 && BB.successors.size() > 1) {
                    criticalEdgeList.add(new CriticalEdge(BB, succ));
                }
            }
        }
        changed = criticalEdgeList.size() > 0;
        if (changed) {
            logger.info("function '" + function.getIdentifier() + "' has " + criticalEdgeList.size() +
                    " critical edges to be split");
        }
        while (!criticalEdgeList.isEmpty()) {
            CriticalEdge edge = criticalEdgeList.pop();
            splitEdge(edge);
        }
        return changed;
    }

    @Override
    public boolean optimize() {
        boolean changed = false;
        for (Function function : TopModule.getFunctionMap().values()) {
            if (!function.isExternal()) {
                changed |= splitEdge(function);
            }
        }
        return changed;
    }
}
