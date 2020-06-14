package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.Instruction;
import IR.Instructions.LoadInst;
import IR.Instructions.StoreInst;
import Optim.FunctionPass;
import Optim.MxOptimizer;

public class Peephole extends FunctionPass {

    private int elimNum = 0;

    public Peephole(Function function) {
        super(function);
    }

    @Override
    public boolean optimize() {
        elimNum = 0;
        for (BasicBlock BB : function.getBlockList()) {
            boolean changed = false;
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

        if (elimNum != 0) {
            MxOptimizer.logger.fine(String.format("Peephole works on function \"%s\", with %d insts eliminated",
                    function.getIdentifier(), elimNum));
        }

        return elimNum != 0;
    }
}
