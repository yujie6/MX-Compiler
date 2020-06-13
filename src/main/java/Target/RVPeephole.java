package Target;

import IR.Function;
import Optim.MxOptimizer;
import Target.RVInstructions.RVInstruction;
import Target.RVInstructions.RVLoad;
import Target.RVInstructions.RVMove;
import Target.RVInstructions.RVStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RVPeephole {

    private RVModule TopModule;

    public RVPeephole(RVModule rvModule) {
        TopModule = rvModule;
    }


    private int elimNum = 0;

    private void removeLoad(RVFunction rvFunction) {

        elimNum = 0;
        for (RVBlock rvBlock : rvFunction.getRvBlockList()) {
            ArrayList<RVInstruction> eraseWorkList = new ArrayList<>();
            HashMap<RVInstruction, RVInstruction> insertAfterMap = new HashMap<>();
            for (int i = 1; i < rvBlock.rvInstList.size(); i++) {
                RVInstruction inst = rvBlock.rvInstList.get(i);
                RVInstruction prev = rvBlock.rvInstList.get(i-1);
                if (inst instanceof RVLoad) {
                    if (prev instanceof RVStore) {
                        if (((RVLoad) inst).getAddr().toString().equals(((RVStore) prev).getAddr().toString())) {
                            if (((RVLoad) inst).getDestReg() != ((RVStore) prev).getSrc()) {
                                insertAfterMap.put(prev, new RVMove(rvBlock, ((RVStore) prev).getSrc(), ((RVLoad) inst).getDestReg()));
                            }
                            eraseWorkList.add(inst);
                            elimNum += 1;
                        }
                    }
                }
            }
            insertAfterMap.forEach(RVInstruction::insertAfterMe);
            eraseWorkList.forEach(RVInstruction::eraseFromParent);
        }
        if (elimNum != 0)
            MxOptimizer.logger.info(String.format("Peephole eliminated %d insts for %s", elimNum,
                rvFunction.getIdentifier() ));
    }

    public void optimize() {
        for (RVFunction rvFunction : TopModule.rvFunctions) {
            removeLoad(rvFunction);
        }
    }

}
