package Target;

import IR.Function;
import IR.Instructions.StoreInst;
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
            }
        }
    }

    private void removeLoadStore(RVFunction rvFunction) {
        rvFunction.getRvBlockList().forEach(BB -> {
            if (BB.rvInstList.size() > 500) return;
            HashMap<String, RVInstruction> localLoadStore = new HashMap<>();
            if (BB.predecessors.size() == 1) {
                RVBlock pred = BB.predecessors.iterator().next();
                for (RVInstruction inst : pred.rvInstList) {
                    if (inst instanceof RVLoad) {
                        if (localLoadStore.containsKey(((RVLoad) inst).getAddr().toString())) {
                            RVInstruction lastInst = localLoadStore.get(((RVLoad) inst).getAddr().toString());
                            if (lastInst instanceof RVStore) {

                            }
                        }
                    } else if (inst instanceof RVStore) {
                        localLoadStore.clear();
                        localLoadStore.put(((RVStore) inst).getAddr().toString(), inst);
                    } else if (inst instanceof RVCall) {
                        localLoadStore.clear();
                    }
                }
            }
        });
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
            removeMove(rvFunction);
            // removeLoadStore(rvFunction);
            removeLoad(rvFunction);
        }
    }

}
