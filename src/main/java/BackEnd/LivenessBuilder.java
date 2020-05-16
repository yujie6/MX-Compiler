package BackEnd;

import Target.*;
import Target.RVInstructions.RVInstruction;

import java.util.HashSet;

/**
 * Implement liveness analysis
 */
public class LivenessBuilder extends RVPass implements AsmVisitor {



    public LivenessBuilder(RVModule topModule) {
        super(topModule);
    }

    @Override
    public Object visit(RVModule rvModule) {
        for (RVFunction function : rvModule.rvFunctions) {
            visit(function);
        }
        return null;
    }

    @Override
    public Object visit(RVFunction rvFunction) {
        for (RVBlock BB : rvFunction.getRvBlockList()) {
            BB.liveOutSet.clear();
            BB.liveInSet.clear();
            visit(BB);
        }

        while (true) {
            boolean changed = false;
            for (RVBlock BB : rvFunction.getRvBlockList()) {
                HashSet<VirtualReg> oldLiveInSet = new HashSet<>(BB.liveInSet);
                HashSet<VirtualReg> oldLiveOutSet = new HashSet<>(BB.liveOutSet);
                BB.liveInSet.clear();
                BB.liveOutSet.clear();
                for (RVBlock pred : BB.predecessors) {
                    BB.liveInSet.addAll(pred.liveOutSet);
                }

                BB.liveOutSet.addAll(BB.gen);
                HashSet<VirtualReg> tmp = new HashSet<>(BB.liveInSet);
                tmp.removeAll(BB.kill);
                BB.liveOutSet.addAll(tmp);
                if (!(oldLiveInSet.equals(BB.liveInSet) && oldLiveOutSet.equals(BB.liveOutSet))) {
                    changed = true;
                }
            }
            if (changed) break;
        }

        return null;
    }

    @Override
    public Object visit(RVBlock rvBlock) {
        for (RVInstruction inst : rvBlock.rvInstList) {
            rvBlock.gen.addAll(inst.getUseRegs());

            if (inst.getDefRegs() != null) {
                rvBlock.kill.addAll(inst.getDefRegs());
            }
        }
        return null;
    }

    @Override
    public void run() {
        visit(TopModule);
    }
}
