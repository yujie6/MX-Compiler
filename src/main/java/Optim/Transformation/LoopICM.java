package Optim.Transformation;

import IR.*;
import IR.Constants.Constant;
import IR.Instructions.*;
import Optim.FuncAnalysis.AliasAnalysis;
import Optim.FuncAnalysis.Loop;
import Optim.FuncAnalysis.LoopAnalysis;
import Optim.FunctionPass;
import com.ibm.icu.util.ICUUncheckedIOException;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Implementing loop invariant code motion
 */
public class LoopICM extends FunctionPass implements IRVisitor {

    private LoopAnalysis LA;
    private AliasAnalysis AA;
    private LinkedList<Instruction> workList;
    private HashSet<Instruction> invariants;
    private Loop curLoop;

    public LoopICM (Function function, LoopAnalysis LA, AliasAnalysis AA) {
        super(function);
        this.LA = LA;
        this.AA = AA;
        workList = new LinkedList<>();
        invariants = new HashSet<>();
    }

    @Override
    public boolean optimize() {
        return true;
        // return hoist(LA.rootLoop);
    }

    private void findInvariant(Loop loop) {
        workList.clear();
        invariants.clear();
        curLoop = loop;
        for (BasicBlock BB : loop.getLoopBlocks()) {
            for (Instruction inst : BB.getInstList()) {
                inst.accept(this);
            }
        }

        while (!workList.isEmpty()) {
            Instruction inst = workList.pop();
            if (invariants.contains(inst)) continue;
            invariants.add(inst);
            for (User user : inst.UserList) {
                ((Instruction) user).accept(this);
            }
        }



    }

    private boolean isInvariant(Value val, Loop loop) {
        if (val instanceof Constant) {
            return true;
        } else if (val instanceof Instruction) {
            if (invariants.contains(val) ) return true;
            return !loop.getLoopBlocks().contains(((Instruction) val).getParent());
        }
        return false;
    }

    private boolean isInvariant(Value val) {
        if (val instanceof Constant) {
            return true;
        } else if (val instanceof Instruction) {
            if (invariants.contains(val) ) return true;
            return !curLoop.getLoopBlocks().contains(((Instruction) val).getParent());
        }
        return false;
    }

    private boolean hoist(Loop loop) {
        // hoist inner loop first
        boolean changed = false;
        curLoop = loop;
        for (Loop insideLoop : loop.insideLoops) {
            if (insideLoop.outerLoop != loop) continue;
            hoist(insideLoop);
        }
        if (loop.outerLoop != null) {
            findInvariant(loop);
            for (Instruction inst : invariants) {
                changed |= hoist(inst, loop);
            }
        }
        return changed;
    }

    private boolean hoist(Instruction inst, Loop loop) {
        boolean changed  = false;
        BasicBlock preHeader = loop.preHeader;
        // hoist instruction to preHeader
//        preHeader.AddInstBeforeBranch(inst);
//        inst.eraseFromParent();
        return changed;
    }

    private boolean notModifiedInLoop(Value pointer, Loop loop) {
        for (BasicBlock BB : loop.getLoopBlocks()) {
            for (Instruction inst : BB.getInstList()) {
                if (AA.getModRefInfo(inst, pointer) != AliasAnalysis.ModRefInfo.NoModRef) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Object visit(BasicBlock node) {
        return null;
    }

    @Override
    public Object visit(Function node) {
        return null;
    }

    @Override
    public Object visit(AllocaInst allocaInst) {
        return null;
    }

    @Override
    public Object visit(BinOpInst binOpInst) {
        Value LHS = binOpInst.getLHS();
        Value RHS = binOpInst.getRHS();
        if (isInvariant(LHS, curLoop) && isInvariant(RHS, curLoop)) {
            workList.add(binOpInst);
        }
        return null;
    }

    @Override
    public Object visit(BitCastInst bitCastInst) {
        if (isInvariant(bitCastInst.getCastValue()) ) {
            workList.add(bitCastInst);
        }
        return null;
    }

    @Override
    public Object visit(BranchInst branchInst) {
        return null;
    }

    @Override
    public Object visit(CallInst callInst) {
        for (Value arg : callInst.getArgumentList()) {
            if (!isInvariant(arg, curLoop)) {
                return null;
            }
        }
        workList.add(callInst);
        return null;
    }

    @Override
    public Object visit(CmpInst cmpInst) {
        if (isInvariant(cmpInst.getLHS()) && isInvariant(cmpInst.getRHS())) {
            workList.add(cmpInst);
        }
        return null;
    }

    @Override
    public Object visit(CopyInst copyInst) {

        return null;
    }

    @Override
    public Object visit(GetPtrInst getPtrInst) {
        for (Value offset : getPtrInst.getOffsets() ) {
            if (!isInvariant(offset)) {
                return null;
            }
        }
        if (notModifiedInLoop(getPtrInst.getAggregateValue(), curLoop)) {
            workList.add(getPtrInst);
        }
        return null;
    }

    @Override
    public Object visit(LoadInst loadInst) {
        Value addr = loadInst.getLoadAddr();
        if (isInvariant(addr) && notModifiedInLoop(addr, curLoop)) {
            workList.add(loadInst);
        }
        return null;
    }

    @Override
    public Object visit(ReturnInst returnInst) {
        return null;
    }

    @Override
    public Object visit(PhiInst phiInst) {
        return null;
    }

    @Override
    public Object visit(SextInst sextInst) {
        return null;
    }

    @Override
    public Object visit(StoreInst storeInst) {
        return null;
    }
}
