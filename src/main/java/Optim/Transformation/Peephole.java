package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.GlobalVariable;
import IR.Instructions.CallInst;
import IR.Instructions.Instruction;
import IR.Instructions.LoadInst;
import IR.Instructions.StoreInst;
import IR.Value;
import Optim.FunctionPass;
import Optim.MxOptimizer;

import java.util.HashMap;
import java.util.List;

public class Peephole extends FunctionPass {

    private int elimNum = 0;

    public Peephole(Function function) {
        super(function);
    }

    private void removeLoadStoreGlobal() {
        for (BasicBlock BB : function.getBlockList()) {
            boolean changed;
            do {
                changed = false;
                HashMap<GlobalVariable, Instruction> globalLoadStore = new HashMap<>();
                if (BB.predecessors.size() == 1) {
                    BasicBlock pred = BB.predecessors.iterator().next();
                    for (Instruction inst : pred.getInstList()) {
                        if (inst instanceof LoadInst) {
                            if (((LoadInst) inst).getLoadAddr() instanceof GlobalVariable) {
                                globalLoadStore.put((GlobalVariable) ((LoadInst) inst).getLoadAddr(), inst);
                            }
                        } else if (inst instanceof StoreInst) {
                            if (((StoreInst) inst).getStoreDest() instanceof GlobalVariable) {
                                globalLoadStore.put((GlobalVariable) ((StoreInst) inst).getStoreDest(), inst);
                            }
                        } else if (inst instanceof CallInst) {
                            globalLoadStore.clear();
                        }
                    }
                }
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

    private void removeLoadStoreLocal() {
        for (BasicBlock BB : function.getBlockList()) {
            boolean changed;
            do {
                changed = false;
                HashMap<Value, Instruction> localLoadStore = new HashMap<>();
                if (BB.predecessors.size() == 1) {
                    BasicBlock pred = BB.predecessors.iterator().next();
                    for (Instruction inst : pred.getInstList()) {
                        if (inst instanceof LoadInst) {
                            localLoadStore.put(((LoadInst) inst).getLoadAddr(), inst);
                        } else if (inst instanceof StoreInst) {
                            // localLoadStore.clear();
                            localLoadStore.put(((StoreInst) inst).getStoreDest(), inst);
                        } else if (inst instanceof CallInst) {
                            localLoadStore.clear();
                        }
                    }
                }
                for (Instruction inst : List.copyOf(BB.getInstList())) {
                    if (inst instanceof LoadInst) {
                        if (localLoadStore.containsKey(((LoadInst) inst).getLoadAddr())) {
                            Instruction lastInst = localLoadStore.get(((LoadInst) inst).getLoadAddr());
                            if (lastInst instanceof LoadInst) {
                                inst.replaceAllUsesWith(lastInst);
                            } else if (lastInst instanceof StoreInst) {
                                inst.replaceAllUsesWith(((StoreInst) lastInst).getStoreValue());
                            }
                            inst.eraseFromParent();
                            changed = true;
                            elimNum += 1;
                        }
                    } else if (inst instanceof StoreInst) {
                        if (localLoadStore.containsKey(((StoreInst) inst).getStoreDest())) {
                            Instruction lastInst = localLoadStore.get(((StoreInst) inst).getStoreDest());
                            if (lastInst instanceof StoreInst) {
                                lastInst.eraseFromParent();
                                elimNum += 1;
                                changed = true;
                            }
                        }
                        // localLoadStore.clear();
                        localLoadStore.put(((StoreInst) inst).getStoreDest(), inst);
                    } else if (inst instanceof CallInst) {
                        localLoadStore.clear();
                    }
                }
            } while (changed);
        }
    }

    private void removeLoadSimple() {
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
        removeLoadStoreGlobal();
        // removeLoadStoreLocal();
        removeLoadSimple();
        if (elimNum != 0) {
            MxOptimizer.logger.warning(String.format("<Peephole> works on function \"%s\", with %d insts eliminated",
                    function.getIdentifier(), elimNum));
        }

        return elimNum != 0;
    }
}
