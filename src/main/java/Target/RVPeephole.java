package Target;

import IR.Function;
import Optim.MxOptimizer;
import Target.RVInstructions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RVPeephole {

    private RVModule TopModule;

    public RVPeephole(RVModule rvModule) {
        TopModule = rvModule;
    }


    private int elimNum = 0;

    private void removeMove(RVFunction rvFunction) {
        for (RVBlock rvBlock : rvFunction.getRvBlockList()) {
            for (RVInstruction inst : List.copyOf(rvBlock.rvInstList)) {
                if (inst instanceof RVMove) {
                    if (((RVMove) inst).getSrc().equals(((RVMove) inst).getDest())) {
                        inst.eraseFromParent();
                        elimNum += 1;
                    }
                }
                /*else if (inst instanceof RVArithImm) {
                    if (inst.getOpcode().equals("addi") && ((RVArithImm) inst).getImm().toString().equals("0")) {
                        inst.eraseFromParent();
                        elimNum += 1;
                    }
                }*/
            }
        }
    }

    private void removeLoad(RVFunction rvFunction) {

        elimNum = 0;
        for (RVBlock rvBlock : rvFunction.getRvBlockList()) {
            if (rvBlock.rvInstList.size() > 500) continue;
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
            MxOptimizer.logger.info(String.format("RV <Peephole> eliminated %d insts for %s", elimNum,
                rvFunction.getIdentifier() ));
    }

    public void optimize() {
        for (RVFunction rvFunction : TopModule.rvFunctions) {
            removeLoad(rvFunction);
            removeMove(rvFunction);
        }
    }

}
