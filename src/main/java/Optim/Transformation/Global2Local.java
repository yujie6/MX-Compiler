package Optim.Transformation;

import IR.*;
import IR.Constants.IntConst;
import IR.Constants.NullConst;
import IR.Instructions.AllocaInst;
import IR.Instructions.Instruction;
import IR.Instructions.StoreInst;
import IR.Module;
import IR.Types.PointerType;
import Optim.FunctionPass;
import Optim.MxOptimizer;
import Optim.Pass;

import java.util.ArrayList;

public class Global2Local extends Pass {

    private int elimNum = 0;
    private Module TopModule;
    public Global2Local(Module TopModule) {
        this.TopModule = TopModule;
    }

    private void eliminateTrivialGlobals() {
        ArrayList<GlobalVariable> globalWorkList = new ArrayList<>();
        for (Value gvar : TopModule.getGlobalVarMap().values()) {
            if (gvar instanceof GlobalVariable && !((GlobalVariable) gvar).isStringConst) {
                GlobalVariable global = (GlobalVariable) gvar;
                Function function = isGlobalPromotable(global);
                if (function != null) {
                    elimNum += 1;
                    globalWorkList.add(global);
                    BasicBlock curBB = function.getHeadBlock();
                    AllocaInst replaceVal = new AllocaInst(curBB, global.getOriginalType());
                    global.replaceAllUsesWith(replaceVal);
                    Value initValue = global.getInitValue();
                    Instruction insertInst = null;
                    for (Instruction inst : curBB.getInstList()) {
                        if (!(inst instanceof AllocaInst)) {
                            insertInst = inst;
                            break;
                        }
                    }
                    if (insertInst == null) {
                        System.out.println("Damn it");
                        System.exit(-1);
                    }
                    if (initValue != null) {
                        StoreInst st = new StoreInst(curBB, initValue, replaceVal);
                        insertInst.addInstBefore(st);
                    } else {
                        if (global.getOriginalType() instanceof PointerType) {
                            insertInst.addInstBefore(new StoreInst(curBB, new NullConst(global.getOriginalType()), replaceVal));
                        } else {
                            insertInst.addInstBefore(new StoreInst(curBB, new IntConst(0), replaceVal));
                        }
                    }
                    curBB.AddInstAtTop(replaceVal);
                }
            }
        }
        globalWorkList.forEach(gvar -> {TopModule.getGlobalVarMap().remove(gvar.getIdentifier()); });

    }

    private void promoteGlobalToLocal() {

    }

    private Function isGlobalPromotable(GlobalVariable globalVariable) {
        Function promotable = null;
        for (User U : globalVariable.UserList) {
            if (U instanceof Instruction) {
                Function parent = ((Instruction) U).getParent().getParent();
                if (promotable == null) {
                    promotable = parent;
                } else if (promotable != parent) {
                    return null;
                }
            }
        }
        return promotable;
    }

    @Override
    public boolean optimize() {
        elimNum = 0;
        eliminateTrivialGlobals();
        promoteGlobalToLocal();
        if (elimNum != 0) {
            MxOptimizer.logger.fine(String.format("Global2local promote %d global variables", elimNum));
        }
        return elimNum != 0;
    }
}
