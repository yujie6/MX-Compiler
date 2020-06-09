package Optim.Transformation;

import IR.*;
import IR.Instructions.BranchInst;
import IR.Instructions.CallInst;
import IR.Instructions.Instruction;
import IR.Instructions.ReturnInst;
import IR.Module;
import Optim.MxOptimizer;
import Optim.Pass;

import java.util.ArrayList;
import java.util.HashSet;

public class FunctionInline extends Pass {

    private Module TopModule;
    private ArrayList<Function> freeToInline;
    private HashSet<CallInst> inlineWorkList;
    private IRMap irMap;

    public FunctionInline(Module TopModule) {
        this.TopModule = TopModule;
        this.freeToInline = new ArrayList<>();
        this.inlineWorkList = new HashSet<>();
        this.irMap = new IRMap();
    }

    public void updateCallGraph() {
        TopModule.getFunctionMap().forEach((s, f) -> {
            f.callee.clear();
            f.caller.clear();
        });
        TopModule.getFunctionMap().forEach((s, f) -> {
            f.instNum = 0;
            for (BasicBlock BB : f.getBlockList()) {
                for (Instruction inst : BB.getInstList()) {
                    if (inst instanceof CallInst) {
                        ((CallInst) inst).getCallee().caller.add(f);
                        f.callee.add(((CallInst) inst).getCallee());
                    }
                }
                f.instNum += BB.getInstList().size();
            }
        });
    }

    private int elimNum = 0, inlineBound = 600;

    private boolean tryInline() {
        HashSet<Function> elimFunctions = new HashSet<>();
        TopModule.getFunctionMap().forEach((s, f) -> {
            if (f.callee.size() == 0 && !f.isExternal()) freeToInline.add(f);
            if (f.caller.size() == 0) elimFunctions.add(f);
        });
        elimFunctions.forEach(f -> {
            TopModule.getFunctionMap().remove(f.getIdentifier());
        });
        elimNum += elimFunctions.size();
        TopModule.getFunctionMap().forEach((s, f) -> {
            f.getBlockList().forEach(BB -> {
                BB.getInstList().forEach(inst -> {
                    if (inst instanceof CallInst) {
                        CallInst callInst = ((CallInst) inst);
                        if (freeToInline.contains(callInst.getCallee()) && callInst.getCallee().instNum < inlineBound) {
                            inlineWorkList.add(callInst);
                        }
                    }
                });
            });
        });
        inlineWorkList.forEach(call -> {
            tryInlineForFunction(call, call.getParent().getParent());
        });
        return elimNum != 0;
    }

    private void tryInlineForFunction(CallInst callInst, Function caller) {
        BasicBlock curBlock = callInst.getParent();
        BasicBlock mergeBlock = curBlock.split(callInst);
        Function callee = callInst.getCallee();
        irMap.clear();
        if (callee.instNum + caller.instNum > inlineBound) return;

        for (int i = 0; i < callee.getParameterList().size(); i++) {
            Argument arg = callee.getParameterList().get(i);
            arg.replaceAllUsesWith(callInst.getArgument(i));
        }
        for (BasicBlock BB : callee.getBlockList()) {
            BasicBlock newBB = new BasicBlock(BB);
            irMap.put(BB, newBB);
        }
        for (BasicBlock BB : callee.getBlockList()) {
            BasicBlock newBB = irMap.get(BB);
            BB.getInstList().forEach(inst-> {inst.copyTo(newBB, irMap);});
        }
        BasicBlock headBB = irMap.get(callee.getHeadBlock());
        BasicBlock retBB = irMap.get(callee.getRetBlock());
        Instruction ret = retBB.getInstList().getLast();
        if (ret instanceof ReturnInst) {
            ret.eraseFromParent();
        } else {
            MxOptimizer.logger.severe("Fatal error");
        }
        retBB.mergeWith(mergeBlock);
        curBlock.mergeWith(headBB);
        Value retValue = irMap.get(callee.getRetValue());
        callInst.replaceAllUsesWith(retValue);
        callInst.eraseFromParent();
    }

    @Override
    public boolean optimize() {
        this.elimNum = 0;
        do {
            updateCallGraph();
        } while (tryInline());
        MxOptimizer.logger.fine("Function inline do inline for " + elimNum + " functions.");
        return false;
    }
}
