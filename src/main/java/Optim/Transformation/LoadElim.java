package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.Instruction;
import IR.Instructions.LoadInst;
import IR.Instructions.StoreInst;
import IR.User;
import IR.Value;
import Optim.FuncAnalysis.AliasAnalysis;
import Optim.FuncAnalysis.DomNode;
import Optim.FuncAnalysis.DomTreeBuilder;
import Optim.FunctionPass;
import Optim.MxOptimizer;

import java.util.HashSet;
import java.util.List;

public class LoadElim extends FunctionPass {

    private AliasAnalysis AA;
    private DomTreeBuilder dm;
    private int elimNum;
    public LoadElim(Function function, AliasAnalysis AA, DomTreeBuilder dm) {
        super(function);
        this.AA = AA;
        this.dm = dm;
    }

    @Override
    public boolean optimize() {
        DomNode root = dm.domTree.get(function.getHeadBlock());
        elimNum = 0;
        visit(root);
        if (elimNum != 0)
            MxOptimizer.logger.fine(String.format("Redundant load elimination works on function \"%s\", with %d insts eliminated",
                function.getIdentifier(), elimNum));
        return elimNum != 0;
    }

    private void visit(DomNode domNode) {
        BasicBlock BB = domNode.block;
        boolean changed = false;
        do {
            changed = false;
            int len = BB.getInstList().size();
            for (int i = 0; i < len; i++) {
                Instruction inst = BB.getInstList().get(i);
                if (inst instanceof LoadInst) {
                    LoadInst elimLoad = null;
                    for (int j = 1; i + j < Math.min(i + 14, len); j++) {
                        Instruction elim = BB.getInstList().get(i + j);
                        if (elim instanceof StoreInst) {
                            break;
                        } else if (elim instanceof LoadInst && ((LoadInst) elim).getLoadAddr() == ((LoadInst) inst).getLoadAddr()) {
                            elimLoad = ((LoadInst) elim);
                        }
                    }
                    if (elimLoad != null) {
                        elimNum += 1;
                        changed = true;
                        elimLoad.replaceAllUsesWith(inst);
                        elimLoad.eraseFromParent();
                        break;
                    }
                }
            }
        } while (changed);
        domNode.children.forEach(child -> { visit(child); });
    }

    private boolean notModified(BasicBlock cur, BasicBlock defBB, Value addr) {
        if (visited.contains(cur)) return true;
        visited.add(cur);

        if (cur == curLoad.getParent()) {
            for (Instruction inst : cur.getInstList()) {
                if (inst == curLoad) break;
                if (AA.getModRefInfo(inst, addr) != AliasAnalysis.ModRefInfo.NoModRef) {
                    return false;
                }
            }
        } else if (cur == defBB) {
            boolean meetCurLoad = false;
            for (Instruction inst : cur.getInstList()) {
                if (inst == curOrigin) {
                    meetCurLoad = true; continue;
                } else if (!meetCurLoad) continue;
                if (AA.getModRefInfo(inst, addr) != AliasAnalysis.ModRefInfo.NoModRef) {
                    return false;
                }
            }
            return true;
        } else {
            for (Instruction inst : cur.getInstList()) {
                if (AA.getModRefInfo(inst, addr) != AliasAnalysis.ModRefInfo.NoModRef) {
                    return false;
                }
            }
        }

        for (BasicBlock pred : cur.predecessors) {
            if (!notModified(pred, defBB, addr)) {
                return false;
            }
        }
        return true;

    }
    private Instruction curLoad, curOrigin;

    private HashSet<BasicBlock> visited = new HashSet<>();

    private boolean isSafeToEliminate(LoadInst other, LoadInst origin) {
        Value addr = origin.getLoadAddr();
        if (dm.dominates(origin, other)) {
            BasicBlock defBB = origin.getParent(), useBB = other.getParent();
            if (defBB == useBB) {
                boolean reachDef = false;
                for (Instruction inst : defBB.getInstList()) {
                    if (inst == origin) {
                        reachDef = true; continue;
                    } else if (!reachDef) {
                        continue;
                    } else if (inst != other) {
                        if (AA.getModRefInfo(inst, addr) != AliasAnalysis.ModRefInfo.NoModRef) {
                            return false;
                        }
                    } else {
                        break;
                    }
                }

            } else {
                visited.clear();
                curLoad = other;
                curOrigin = origin;
                return notModified(useBB, defBB, addr);
            }
        }
        return false;
    }
}
