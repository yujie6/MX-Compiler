package BackEnd;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.BranchInst;
import IR.Instructions.CopyInst;
import IR.Instructions.Instruction;
import IR.Instructions.PhiInst;
import IR.Module;
import IR.Value;
import Optim.ModulePass;
import Tools.MXLogger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Prior to SSADestruction, we need to prevent the case
 * that a -> b, while a.succ.size() > 1 && b.pred.size() > 1
 * by simply adding a new cfg node z, a -> z -> b
 */
public class EdgeSplitter extends ModulePass {

    private LinkedList<CriticalEdge> criticalEdgeList;
    private HashMap<CriticalEdge, BasicBlock> splitBBMap;
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
        this.splitBBMap = new HashMap<>();
        this.logger = logger;
    }

    private void splitEdge(CriticalEdge edge) {
        BasicBlock splitter = new BasicBlock(edge.getParent(), "edge_splitter");
        BranchInst branch = (BranchInst) edge.A.getTailInst();
        splitBBMap.put(edge, splitter);
        branch.replaceSuccBlock(edge.B, splitter);
        splitter.AddInstAtTail(new BranchInst(splitter, null, edge.B, null));
    }

    private boolean splitEdge(Function function) {
        int splitEdgeNum = 0;
        int copyInstNum = 0;
        for (BasicBlock BB : function.getBlockList()) {
            splitEdgeNum += criticalEdgeList.size();
            criticalEdgeList.clear();
            for (BasicBlock pred : BB.predecessors) {
                if (pred.predecessors.size() > 1 && BB.successors.size() > 1) {
                    criticalEdgeList.add(new CriticalEdge(pred, BB));
                }
            }

            for (CriticalEdge edge : criticalEdgeList) {
                splitEdge(edge);
            }

            for (Instruction inst : BB.getInstList()) {
                if (inst instanceof PhiInst) {
                    PhiInst phi = (PhiInst) inst;
                    copyInstNum += phi.getBranchNum();
                    for (int i = 0; i < phi.getBranchNum(); i++) {
                        BasicBlock pred = phi.getBlock(i);
                        Value copyValue = phi.getValue(i);
                        CopyInst copyInst = new CopyInst(pred, phi, copyValue, true);
                        pred.addCopyInst(copyInst);
                        pred.AddInstBeforeBranch(copyInst);
                    }
                }
            }
        }

        for (var entry : splitBBMap.entrySet()) {
            function.AddBlockAfter(entry.getKey().A, entry.getValue());
        }
        splitBBMap.clear();


        if (splitEdgeNum > 0) {
            logger.info("function '" + function.getIdentifier() + "' has " + splitEdgeNum +
                    " critical edges to be split");
            logger.info("function '" + function.getIdentifier() + "' has " + copyInstNum +
                    " copy insts added");
            return true;
        }
        return false;
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
