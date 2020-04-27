package Optim.Transformation;

import IR.BasicBlock;
import IR.Constants.Constant;
import IR.Function;
import IR.Instructions.BinOpInst;
import IR.Instructions.Instruction;
import IR.User;
import IR.Value;
import Optim.FuncAnalysis.Loop;
import Optim.FuncAnalysis.LoopAnalysis;
import Optim.FunctionPass;
import Optim.Pass;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Implementing loop invariant code motion
 */
public class LoopICM extends FunctionPass {

    private LoopAnalysis LA;
    private LinkedList<Instruction> workList;
    private HashSet<Instruction> invariants;

    public LoopICM (Function function, LoopAnalysis LA) {
        super(function);
        this.LA = LA;
        workList = new LinkedList<>();
        invariants = new HashSet<>();
    }

    @Override
    public boolean optimize() {
        return hoist(LA.rootLoop);
    }

    private void findInvariant(Loop loop) {
        workList.clear();
        invariants.clear();
        for (BasicBlock BB : loop.getLoopBlocks()) {
            for (Instruction inst : BB.getInstList()) {
                check(inst, loop);
            }
        }

        while (!workList.isEmpty()) {
            Instruction inst = workList.pop();
            invariants.add(inst);
            for (User user : inst.UserList) {
                check((Instruction) user, loop);
            }
        }



    }

    private void check(Instruction inst, Loop loop) {
        if (inst instanceof BinOpInst) {
            Value LHS = ((BinOpInst) inst).getLHS();
            Value RHS = ((BinOpInst) inst).getRHS();
            if (isInvariant(LHS, loop) && isInvariant(RHS, loop)) {
                workList.add(inst);
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

    private boolean hoist(Loop loop) {
        // hoist inner loop first
        boolean changed = false;
        for (Loop insideLoop : loop.insideLoops) {
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
        return changed;
    }
}
