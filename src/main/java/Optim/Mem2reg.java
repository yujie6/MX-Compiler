package Optim;

import IR.*;
import IR.Instructions.*;
import IR.Module;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This file exposes an interface to promote alloca instructions to SSA
 * registers, by using the SSA construction algorithm.
 */
public class Mem2reg extends Pass {

    private Module TopModule;
    ArrayList<AllocaInst> allocaInsts;
    private int NumPromoted;

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
            for (Use use : AI.getUseList()) {
                User U = use.getUser();
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


    public Mem2reg(Module topModule) {
        this.TopModule = topModule;
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

            if (allocaInsts.isEmpty())
                break;

            PromoteMemToReg(allocaInsts);
            NumPromoted += allocaInsts.size();
            changed = true;
        }
        return changed;
    }

    private boolean PromoteMemToReg(ArrayList<AllocaInst> allocaInsts) {
        boolean changed = false;
        AllocaInfo info = new AllocaInfo();
        for (AllocaInst AI : allocaInsts) {
            info.AnalyzeAlloca(AI);

            if (info.definingBlocks.size() == 1)
                rewriteSingleStoreAlloca(AI, info);

            if (info.onlyUsedInOneBlock) {

            }
        }
        return changed;
    }

    /*Rewrite as many loads as possible given a single store.
    When there is only a single store, we can use the domtree to trivially
    replace all of the dominated loads with the stored value. If this returns
    false there were some loads which were not dominated by the single store
    and thus must be phi-ed with undef. We fall back to the standard alloca
    promotion algorithm in that case.*/
    private void rewriteSingleStoreAlloca(AllocaInst AI, AllocaInfo info) {

    }

    @Override
    boolean optimize() {
        boolean changed = false;
        for (Function func : TopModule.getFunctionMap().values()) {
            changed |= (boolean) visit(func);
        }
        return changed;
    }

    @Override
    public Object visit(BasicBlock node) {
        return null;
    }

    @Override
    public Object visit(Function node) {
        boolean changed = false;
        changed = promoteMemoryToRegister(node);
        return changed;
    }
}
