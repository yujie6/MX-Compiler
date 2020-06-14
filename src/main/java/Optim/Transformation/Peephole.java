package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.GlobalVariable;
import IR.Instructions.CallInst;
import IR.Instructions.Instruction;
import IR.Instructions.LoadInst;
import IR.Instructions.StoreInst;
import Optim.FunctionPass;
import Optim.MxOptimizer;

import java.util.HashMap;
import java.util.List;

public class Peephole extends FunctionPass {

    private int elimNum = 0;

    public Peephole(Function function) {
        super(function);
    }

    private void removeLoadStore() {
        for (BasicBlock BB : function.getBlockList()) {
            boolean changed;
            do {
                changed = false;
                HashMap<GlobalVariable, Instruction> globalLoadStore = new HashMap<>();
                /*if (BB.predecessors.size() == 1) {

                }*/
                for (Instruction inst : List.copyOf(BB.getInstList())) {
                    if (inst instanceof LoadInst) {
                        if (((LoadInst) inst).getLoadAddr() instanceof GlobalVariable &&
                        globalLoadStore.containsKey(((LoadInst) inst).getLoadAddr()) ) {
                            GlobalVariable gvar = (GlobalVariable) ((LoadInst) inst).getLoadAddr();
                            Instruction lastInst = globalLoadStore.get(gvar);
                            if (lastInst instanceof LoadInst) {
                                inst.replaceAllUsesWith(lastInst);
                            } else if (lastInst instanceof StoreInst) {
                                inst.replaceAllUsesWith(((StoreInst) lastInst).getStoreValue());
                            }
                            changed = true;
                            inst.eraseFromParent();
                            elimNum += 1;
                        } else {
                            if (((LoadInst) inst).getLoadAddr() instanceof GlobalVariable) {
                                globalLoadStore.put((GlobalVariable) ((LoadInst) inst).getLoadAddr(), inst);
                            } else {

                            }
                        }
                    } else if (inst instanceof StoreInst) {
                        if (((StoreInst) inst).getStoreDest() instanceof GlobalVariable &&
                        globalLoadStore.containsKey(((StoreInst) inst).getStoreDest())) {
                            GlobalVariable gvar = (GlobalVariable) ((StoreInst) inst).getStoreDest();
                            Instruction lastInst = globalLoadStore.get(gvar);
                            if (lastInst instanceof StoreInst) {
                                lastInst.eraseFromParent();
                                changed = true;
                                elimNum += 1;
                            }
                        }
                        if (((StoreInst) inst).getStoreDest() instanceof GlobalVariable) {
                            globalLoadStore.put((GlobalVariable) ((StoreInst) inst).getStoreDest(), inst);
                        } else {

                        }
                    } else if (inst instanceof CallInst) {
                        globalLoadStore.clear();
                    }
                }
            } while (changed);

        }
    }

    private void removeLoad() {
        for (BasicBlock BB : function.getBlockList()) {
            boolean changed;
            do {
                changed = false;
                int len = BB.getInstList().size();
                for (int i = 0; i < len; i++) {
                    Instruction inst = BB.getInstList().get(i);
                    if (inst instanceof StoreInst) {
                        LoadInst elimLoad = null;
                        for (int j = 1; i + j < Math.min(i + 14, len); j++) {
                            Instruction elim = BB.getInstList().get(i + j);
                            if (elim instanceof StoreInst) {
                                break;
                            } else if (elim instanceof LoadInst) {
                                if (((LoadInst) elim).getLoadAddr() == ((StoreInst) inst).getStoreDest()) {
                                    elimLoad = ((LoadInst) elim);
                                    break;
                                }
                            }
                        }
                        if (elimLoad != null) {
                            elimNum += 1;
                            changed = true;
                            elimLoad.replaceAllUsesWith(((StoreInst) inst).getStoreValue());
                            elimLoad.eraseFromParent();
                            break;
                        }
                    }
                }
            } while (changed);

        }
    }

    @Override
    public boolean optimize() {
        elimNum = 0;
        removeLoadStore();
        removeLoad();
        if (elimNum != 0) {
            MxOptimizer.logger.fine(String.format("<Peephole> works on function \"%s\", with %d insts eliminated",
                    function.getIdentifier(), elimNum));
        }

        return elimNum != 0;
    }
}
