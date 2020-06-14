package Optim.Transformation;

import IR.*;
import IR.Instructions.*;
import IR.Module;
import Optim.FuncAnalysis.DomNode;
import Optim.FuncAnalysis.DomTreeBuilder;
import Optim.MxOptimizer;
import Optim.Pass;
import org.antlr.v4.tool.ast.BlockAST;

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

    private int elimNum = 0, inlineBound = 600;

    private boolean tryInline() {
        this.freeToInline.clear();
        this.inlineWorkList.clear();
        this.elimNum = 0;
        HashSet<Function> elimFunctions = new HashSet<>();
        TopModule.getFunctionMap().forEach((s, f) -> {
            if (f.callee.size() == 0 && !f.isExternal()) freeToInline.add(f);
            if (f.caller.size() == 0 && !f.getIdentifier().equals("main")) elimFunctions.add(f);
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

        MxOptimizer.logger.info("Function inline do inline for " + elimNum + " function calls.");
        changed |= elimNum != 0;
        return elimNum != 0;
    }

    private ArrayList<BasicBlock> visited = new ArrayList<>();

    private void buildNewBB(DomNode domNode, BasicBlock mergeBlock, Function caller) {

        BasicBlock BB = domNode.block;
        if (visited.contains(BB)) return;
        visited.add(BB);

        BasicBlock newBB = irMap.get(BB);
        BB.getInstList().forEach(inst -> {
            inst.copyTo(newBB, irMap);
            irMap.put(inst, newBB.getTailInst());
        });
        caller.AddBlockBefore(mergeBlock, newBB);
        domNode.children.forEach( child -> {buildNewBB(child, mergeBlock, caller);});
    }

    private void tryInlineForFunction(CallInst callInst, Function caller) {
        BasicBlock curBlock = callInst.getParent();
        Function callee = callInst.getCallee();
        irMap.clear();
        if (callee.instNum + caller.instNum > inlineBound) return;
        BasicBlock mergeBlock = curBlock.split(callInst);
        this.elimNum += 1;
        for (int i = 0; i < callee.getParameterList().size(); i++) {
            Argument arg = callee.getParameterList().get(i);
            irMap.put(arg, callInst.getArgument(i));
        }
        for (BasicBlock BB : callee.getBlockList()) {
            BasicBlock newBB = new BasicBlock(BB);
            newBB.setParent(caller);
            irMap.put(BB, newBB);
        }
        /*DomNode root = callee.dm.domTree.get(callee.getHeadBlock());
        visited.clear();
        buildNewBB(root, mergeBlock, caller);*/
        for (BasicBlock BB : callee.getBlockList()) {
            BasicBlock newBB = irMap.get(BB);
            BB.getInstList().forEach(inst -> {
                inst.copyTo(newBB, irMap);
                irMap.put(inst, newBB.getTailInst());
            });
            caller.AddBlockBefore(mergeBlock, newBB);
        }

        for (BasicBlock BB : callee.getBlockList()) {
            BB.getInstList().forEach(inst -> {
                if (inst instanceof PhiInst) {
                    ((PhiInst) inst).removePlaceHolder(irMap);
                }
            });
        }

        BasicBlock headBB = irMap.get(callee.getHeadBlock());
        BasicBlock retBB = irMap.get(callee.getRetBlock());
        Instruction ret = callee.getRetBlock().getInstList().getLast();
        if (ret instanceof ReturnInst) {
            if (!callInst.isVoid())
                callee.setRetValue(((ReturnInst) ret).getRetValue());
            Instruction newRet = (Instruction) irMap.get(ret);
            newRet.eraseFromParent();
        } else {
            MxOptimizer.logger.severe("Fatal error");
        }
        retBB.mergeWith(mergeBlock);
        curBlock.mergeWith(headBB);
        if (!callInst.isVoid()) {
            Value retValue = irMap.get(callee.getRetValue());
            callInst.replaceAllUsesWith(retValue);
        }
        MxOptimizer.logger.info(caller.getIdentifier() +" called [inline] " + callee.getIdentifier());
        callInst.eraseFromParent();
    }

    boolean changed = false;

    @Override
    public boolean optimize() {
        changed = false;
        do {
            TopModule.updateCallGraph();
        } while (tryInline());
        return changed;
    }
}
