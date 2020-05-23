package Optim.Transformation;

import IR.*;
import IR.Instructions.*;
import Optim.FuncAnalysis.DomTreeBuilder;
import Optim.FunctionPass;
import Optim.MxOptimizer;
import Optim.Pass;

import java.util.*;

/**
 * This file exposes an interface to promote alloca instructions to SSA
 * registers, by using the SSA construction algorithm.
 */
public class Mem2reg extends FunctionPass {

    ArrayList<AllocaInst> allocaInsts;
    private int NumPromoted;
    DomTreeBuilder domBuilder;
    private Set<BasicBlock> visited;
    private HashMap<BasicBlock, HashMap<AllocaInst, PhiInst>> NewPhiNodes = new HashMap<>();

    public Mem2reg(Function function1, DomTreeBuilder dm) {
        super(function1);
        this.domBuilder = dm;
        this.NumPromoted = 0;
        visited = new HashSet<>();
        allocaInsts = new ArrayList<>();
    }

    protected class AllocaInfo {
        Set<BasicBlock> definingBlocks;
        Set<BasicBlock> usingBlocks;
        StoreInst onlyStore;
        LoadInst onlyLoad;
        BasicBlock onlyBlock;
        boolean onlyUsedInOneBlock;
        boolean isStoreOnce;

        public AllocaInfo() {
            definingBlocks = new LinkedHashSet<>();
            usingBlocks = new LinkedHashSet<>();
        }

        public void clear() {
            definingBlocks.clear();
            usingBlocks.clear();
            onlyLoad = null;
            onlyStore = null;
            isStoreOnce = true;
            onlyUsedInOneBlock = true;
        }

        public void AnalyzeAlloca(AllocaInst AI) {
            clear();
            for (User U : AI.UserList) {
                if (U instanceof StoreInst) {
                    if (onlyStore != null) isStoreOnce = false;
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
        for (User U : AI.UserList) {
            if (U instanceof StoreInst) {
                if (((StoreInst) U).getStoreValue() == AI)
                    return false;
            } else if (U instanceof GetPtrInst) {
                return ((GetPtrInst) U).hasAllZeroOffsets();
            } else if (!(U instanceof LoadInst)) {
                return false;
            }
        }
        return !AI.getBaseType().isClassType();
    }

    private boolean promoteMemoryToRegister(Function func) {
        boolean changed = false;
        BasicBlock head = func.getHeadBlock();
        for (Instruction inst : head.getInstList()) {
            if (inst instanceof AllocaInst)
                if (isAllocaPromotable((AllocaInst) inst))
                    allocaInsts.add((AllocaInst) inst);
        }

        NumPromoted = allocaInsts.size();
        changed |= PromoteMemToReg();
        NumPromoted += allocaInsts.size();
        return changed;
    }

    private void RemoveFromAllocasList(int AllocaIdx) {
        allocaInsts.set(AllocaIdx, allocaInsts.get(allocaInsts.size() - 1));
        allocaInsts.remove(allocaInsts.size() - 1);
    }

    private void RenamePass(BasicBlock BB, BasicBlock father, AllocaInst AI, Value replaceValue) {
        var phiSets = NewPhiNodes.get(BB);
        Value newReplaceVal = replaceValue;
        if (phiSets != null) {
            PhiInst phi = phiSets.get(AI);
            if (phi != null) {
                phi.AddPhiBranch(father, replaceValue);
                newReplaceVal = phi;
            }
        }
        if (visited.contains(BB)) return;
        visited.add(BB);
        for (Instruction inst : List.copyOf(BB.getInstList())) {
            if (inst instanceof LoadInst && ((LoadInst) inst).getLoadAddr() == AI) {
                // we can assert "store comes first" or "there is phi inst"
                if (newReplaceVal == null) {
                    MxOptimizer.logger.severe("Mem2reg fatal error, replace val cannot be null!");
                    System.exit(1);
                }
                inst.replaceAllUsesWith(newReplaceVal);
                inst.eraseFromParent();
            }
            if (inst instanceof StoreInst && ((StoreInst) inst).getStoreDest() == AI) {
                newReplaceVal = ((StoreInst) inst).getStoreValue();
                inst.eraseFromParent();
            }
        }
        for (BasicBlock succ : BB.successors) {
            RenamePass(succ, BB, AI, newReplaceVal);
        }
    }

    private boolean PromoteMemToReg() {
        boolean changed = false;
        AllocaInfo info = new AllocaInfo();
        for (int i = 0; i < allocaInsts.size(); i++) {
            AllocaInst AI = allocaInsts.get(i);

            info.AnalyzeAlloca(AI);

            if (AI.UserList.isEmpty()) {
                AI.eraseFromParent();
                RemoveFromAllocasList(i);
                i--;
                continue;
            }

            if (info.definingBlocks.size() == 1 && info.isStoreOnce) {
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


            Set<BasicBlock> liveSet = ComputeLiveInBlocks(AI, info);
            Set<BasicBlock> phiBlocks = ComputePhiBlocks(AI, info, liveSet);

            for (BasicBlock BB : phiBlocks) {
                QueuePhiNode(BB, AI);
            }
        }

        // Renaming for phi & other insts
        for (AllocaInst AI : allocaInsts) {
            visited.clear();
            RenamePass(function.getHeadBlock(), null, AI, null);
            AI.eraseFromParent();
        }

        // EliminateNullPhi();
        return allocaInsts.size() > 0;
    }

    private void EliminateNullPhi() {
        for (var sets : NewPhiNodes.values()) {
            for (PhiInst phi : sets.values()) {

            }
        }
    }

    // Determine which blocks the value is live in, i.e. need phi node.
    public Set<BasicBlock> ComputeLiveInBlocks(AllocaInst AI, AllocaInfo info) {
        Set<BasicBlock> liveSet = new LinkedHashSet<>();
        LinkedList<BasicBlock> liveWaitList = new LinkedList<>();
        for (BasicBlock BB : info.usingBlocks) {
            if (!info.definingBlocks.contains(BB)) {
                liveWaitList.add(BB);
                continue;
            }
            // BB is also defBlock
            for (Instruction inst : BB.getInstList()) {
                if (inst instanceof StoreInst) {
                    if (((StoreInst) inst).getStoreDest() != AI) {
                        continue;
                    } else break; // def on AI before use, not live here
                }
                if (inst instanceof LoadInst) {
                    liveWaitList.add(BB);
                    break;
                }
            }
        }

        while (!liveWaitList.isEmpty()) {
            // need to check predecessor
            BasicBlock BB = liveWaitList.poll();

            if (!liveSet.contains(BB)) {
                liveSet.add(BB);
            } else continue;

            for (BasicBlock pred : BB.predecessors) {
                if (!info.definingBlocks.contains(pred)) {
                    liveWaitList.add(pred);
                }
            }
        }
        return liveSet;
    }

    public Set<BasicBlock> ComputePhiBlocks(AllocaInst AI, AllocaInfo info, Set<BasicBlock> liveSet) {
        Set<BasicBlock> phiBlocks = new LinkedHashSet<>();
        LinkedList<BasicBlock> phiWaitList = new LinkedList<>(info.definingBlocks);
        while (!phiWaitList.isEmpty()) {
            BasicBlock BB = phiWaitList.poll();
            Set<BasicBlock> DF = domBuilder.domFrontier.get(BB);

            for (BasicBlock Y : DF) {
                if (!phiBlocks.contains(Y)) {
                    if (liveSet.contains(Y)) {
                        phiBlocks.add(Y);
                    }
                    if (!(info.definingBlocks.contains(Y))) {
                        if (BB != Y) phiWaitList.add(Y);
                    }
                }
            }
        }
        return phiBlocks;
    }

    public void QueuePhiNode(BasicBlock BB, AllocaInst AI) {
        NewPhiNodes.computeIfAbsent(BB, basicBlock -> new HashMap<>());
        PhiInst phi =  new PhiInst(BB, AI.getBaseType());
        BB.AddInstAtTop(phi);
        NewPhiNodes.get(BB).put(AI, phi);
    }



    /*Rewrite as many loads as possible given a single store.
    When there is only a single store, we can use the domtree to trivially
    replace all of the dominated loads with the stored value. Else insert phi node*/
    private boolean rewriteSingleStoreAlloca(AllocaInst AI, AllocaInfo info) {
        StoreInst onlyStore = info.onlyStore;
        BasicBlock storeBB = info.onlyStore.getParent();
        info.usingBlocks.clear();
        for (User U : AI.UserList) {
            if (!(U instanceof LoadInst)) continue;
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
        // replace each load with the nearest store
        BasicBlock BB = info.onlyBlock;
        Value replaceVal = null;
        for (Instruction inst : List.copyOf(BB.getInstList())) {
            if (inst instanceof LoadInst && ((LoadInst) inst).getLoadAddr() == AI) {
                inst.replaceAllUsesWith(replaceVal);
                inst.eraseFromParent();
            } else if (inst instanceof StoreInst && ((StoreInst) inst).getStoreDest() == AI) {
                replaceVal = ((StoreInst) inst).getStoreValue();
                inst.eraseFromParent();
            }
        }
        AI.eraseFromParent();
        return replaceVal != null;
    }

    @Override
    public boolean optimize() {
        boolean success = promoteMemoryToRegister(function);
        if (success)
            MxOptimizer.logger.info("Mem2reg runs on function '" + function.getIdentifier() + "', with "
            + NumPromoted + " alloca promoted!");
        return success;
    }
}
