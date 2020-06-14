package Optim.Transformation;

import IR.*;
import IR.Constants.IntConst;
import IR.Constants.NullConst;
import IR.Instructions.AllocaInst;
import IR.Instructions.Instruction;
import IR.Instructions.LoadInst;
import IR.Instructions.StoreInst;
import IR.Module;
import IR.Types.PointerType;
import Optim.MxOptimizer;
import Optim.Pass;

import java.util.ArrayList;

public class Global2Local extends Pass {

    private int elimNum = 0;
    private Module TopModule;
    public Global2Local(Module TopModule) {
        this.TopModule = TopModule;
    }

    private Instruction getInsertPlace(BasicBlock head) {
        Instruction insertInst = null;
        for (Instruction inst : head.getInstList()) {
            if (!(inst instanceof AllocaInst)) {
                insertInst = inst;
                break;
            }
        }
        return insertInst;
    }

    private void eliminateTrivialGlobals() {
        ArrayList<GlobalVariable> globalWorkList = new ArrayList<>();
        for (Value gvar : TopModule.getGlobalVarMap().values()) {
            if (gvar instanceof GlobalVariable && !((GlobalVariable) gvar).isStringConst) {
                GlobalVariable global = (GlobalVariable) gvar;
                Function function = isGlobalPromotable(global);

                if (function != null) {
                    if (!function.getIdentifier().equals("main")) continue;
                    elimNum += 1;
                    globalWorkList.add(global);
                    BasicBlock curBB = function.getHeadBlock();
                    AllocaInst replaceVal = new AllocaInst(curBB, global.getOriginalType());
                    global.replaceAllUsesWith(replaceVal);
                    Value initValue = global.getInitValue();
                    Instruction insertInst = getInsertPlace(curBB);
                    if (insertInst == null) {
                        System.out.println("Damn it");
                        System.exit(-1);
                    }
                    if (initValue != null) {
                        StoreInst st = new StoreInst(curBB, initValue, replaceVal);
                        insertInst.addInstBeforeMe(st);
                    } else {
                        if (global.getOriginalType() instanceof PointerType) {
                            insertInst.addInstBeforeMe(new StoreInst(curBB, new NullConst(global.getOriginalType()), replaceVal));
                        } else {
                            insertInst.addInstBeforeMe(new StoreInst(curBB, new IntConst(0), replaceVal));
                        }
                    }
                    curBB.AddInstAtTop(replaceVal);
                }
            }
        }
        globalWorkList.forEach(gvar -> {TopModule.getGlobalVarMap().remove(gvar.getIdentifier()); });

    }

    private void promoteOnFunction(Function function, GlobalVariable gvar) {
        // add a load at first BB, and store at last BB
        BasicBlock head = function.getHeadBlock();
        AllocaInst replaceVal = new AllocaInst(head, gvar.getOriginalType());
        Instruction insertInst = getInsertPlace(head);
        gvar.replaceAllUsesWith(replaceVal, function);

        LoadInst ldGvar = new LoadInst(head, gvar.getOriginalType(), gvar);
        StoreInst stGvar = new StoreInst(head, ldGvar, replaceVal);
        insertInst.addInstBeforeMe(ldGvar);
        insertInst.addInstBeforeMe(stGvar);
        // replace all gvar with alloca (only in this function!!)
        BasicBlock retBB = function.getRetBlock();
        LoadInst ld = new LoadInst(retBB, gvar.getOriginalType(), replaceVal);
        StoreInst st = new StoreInst(retBB, ld, gvar);
        retBB.AddInstBeforeBranch(ld);
        retBB.AddInstBeforeBranch(st);
    }

    private void promoteGlobalToLocal() {
        for (Function function : TopModule.getFunctionMap().values()) {
            if (function.callee.isEmpty()) {

            }
        }
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
        // promoteGlobalToLocal();
        if (elimNum != 0) {
            MxOptimizer.logger.fine(String.format("Global2local promote %d global variables", elimNum));
        }
        return elimNum != 0;
    }
}
