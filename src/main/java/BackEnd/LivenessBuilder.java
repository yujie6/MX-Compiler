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
                BB.liveInSet.addAll(oldLiveOutSet);
                BB.liveInSet.removeAll(BB.kill);
                BB.liveInSet.addAll(BB.gen);
                for (RVBlock succ : BB.successors) {
                    BB.liveOutSet.addAll(succ.liveInSet);
                }
                if (!(oldLiveInSet.equals(BB.liveInSet) && oldLiveOutSet.equals(BB.liveOutSet))) {
                    changed = true;
                }
            }
            if (!changed) break;
        }

        return null;
    }

    @Override
    public Object visit(RVBlock rvBlock) {
        rvBlock.gen.clear();
        rvBlock.kill.clear();
        for (RVInstruction inst : rvBlock.rvInstList) {
            rvBlock.gen.addAll(new HashSet<>(inst.getUseRegs()) {{removeAll(rvBlock.kill); }});
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
