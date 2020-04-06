package Optim;

import IR.*;
import IR.Instructions.*;
import Optim.FuncAnalysis.DomTreeBuilder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This file exposes an interface to promote alloca instructions to SSA
 * registers, by using the SSA construction algorithm.
 */
public class Mem2reg extends Pass {

    private Function function;
    ArrayList<AllocaInst> allocaInsts;
    private int NumPromoted;
    DomTreeBuilder domBuilder;

    public Mem2reg(Function function1, DomTreeBuilder dm) {
        this.function = function1;
        this.domBuilder = dm;
    }

    protected class AllocaInfo {
        Set<BasicBlock> definingBlocks;
        Set<BasicBlock> usingBlocks;
        StoreInst onlyStore;
        LoadInst onlyLoad;
        BasicBlock onlyBlock;
        boolean onlyUsedInOneBlock;

        public AllocaInfo() {
            definingBlocks = new LinkedHashSet<>();
            usingBlocks = new LinkedHashSet<>();
        }

        public void clear() {
            definingBlocks.clear();
            usingBlocks.clear();
            onlyLoad = null;
            onlyStore = null;
            onlyUsedInOneBlock = true;
        }

        public void AnalyzeAlloca(AllocaInst AI) {
            clear();
            for (User U : AI.UserList) {
                if (U instanceof StoreInst) {
                    onlyStore = (StoreInst) U;
                    definingBlocks.add(((StoreInst) U).getParent());
                } else if (U instanceof LoadInst) {
                    usingBlocks.add(((LoadInst) U).getParent());
                }
                if (onlyUsedInOneBlock) {
                    if (onlyBlock == null)
                        onlyBlock = ((Instruction) U).getParent();
                    else if (onlyBlock != ((Instruction) U).getParent())
                        onlyUsedInOneBlock = false;
                }
            }
        }
    }

    private boolean isAllocaPromotable(AllocaInst AI) {
        for (Use use : AI.UseList) {
            User U = use.getUser();
            if (U instanceof StoreInst) {
                if (((StoreInst) U).getStoreValue() == AI)
                    return false;
            } else if (U instanceof GetPtrInst) {
                return ((GetPtrInst) U).hasAllZeroOffsets();
            } else if (!(U instanceof LoadInst)) {
                return false;
            }
        }
        return true;
    }

    private boolean promoteMemoryToRegister(Function func) {
        boolean changed = false;
        BasicBlock head = func.getHeadBlock();
        ArrayList<AllocaInst> allocaInsts = new ArrayList<>();
        for (Instruction inst : head.getInstList()) {
            if (inst instanceof AllocaInst)
                if (isAllocaPromotable((AllocaInst) inst))
                    allocaInsts.add((AllocaInst) inst);
        }


        changed |= PromoteMemToReg(allocaInsts);
        NumPromoted += allocaInsts.size();
        return changed;
    }

    void RemoveFromAllocasList(int AllocaIdx) {
        allocaInsts.set(AllocaIdx, allocaInsts.get(allocaInsts.size() - 1));
        allocaInsts.remove(allocaInsts.size() - 1);
    }

    private boolean PromoteMemToReg(ArrayList<AllocaInst> allocaInsts) {
        boolean changed = false;
        AllocaInfo info = new AllocaInfo();
        for (int i = 0; i < allocaInsts.size(); i++) {
            AllocaInst AI = allocaInsts.get(i);

            info.AnalyzeAlloca(AI);

            if (AI.getUseList().isEmpty()) {
                AI.eraseFromParent();
                RemoveFromAllocasList(i);
                i--;
                continue;
            }

            if (info.definingBlocks.size() == 1) {
                boolean success = rewriteSingleStoreAlloca(AI, info);
                if (success) {
                    RemoveFromAllocasList(i);
                    i--;
                    continue;
                }
            }

            if (info.onlyUsedInOneBlock) {
                boolean success = promoteSingleBlockAlloca(AI, info);
                if (success) {
                    RemoveFromAllocasList(i);
                    i--;
                    continue;
                }
            }

            // prepare for algorithm for adding phi inst
            ComputeLiveInBlocks(AI, info);
            // computing phi blocks
        }
        return changed;
    }

    public void ComputeLiveInBlocks(AllocaInst AI, AllocaInfo info) {

    }

    /*Rewrite as many loads as possible given a single store.
    When there is only a single store, we can use the domtree to trivially
    replace all of the dominated loads with the stored value. Else insert phi node*/
    private boolean rewriteSingleStoreAlloca(AllocaInst AI, AllocaInfo info) {
        StoreInst onlyStore = info.onlyStore;
        BasicBlock storeBB = info.onlyStore.getParent();
        info.usingBlocks.clear();
        for (User U : AI.UserList) {
            LoadInst LI = (LoadInst) U;
            if (LI.getParent() == storeBB) {
                int i1 = storeBB.getInstList().indexOf(LI);
                int i2 = storeBB.getInstList().indexOf(onlyStore);
                if (i1 < i2) {
                    // can't handle
                    info.usingBlocks.add(storeBB);
                    continue;
                }
            } else if (!domBuilder.dominates(storeBB, LI.getParent())) {
                info.usingBlocks.add(LI.getParent());
                continue;
            }

            Value replaceValue = onlyStore.getStoreValue();
            LI.replaceAllUsesWith(replaceValue);
            LI.eraseFromParent();
        }

        if (!info.usingBlocks.isEmpty()) {
            return false;
        }
        // remove dead code
        onlyStore.eraseFromParent();
        AI.eraseFromParent();
        return true;
    }

    public boolean promoteSingleBlockAlloca(AllocaInst AI, AllocaInfo info) {

        return true;
    }

    @Override
    public boolean optimize() {
        return promoteMemoryToRegister(function);
    }
}
