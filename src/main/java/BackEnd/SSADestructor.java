package BackEnd;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.CopyInst;
import IR.Instructions.Instruction;
import IR.Instructions.PhiInst;
import IR.Module;
import IR.Types.LocalRegister;
import Optim.ModulePass;
import Optim.Pass;
import Tools.MXLogger;

import java.util.LinkedList;

/**
 * Parallel Copy to sequential !
 */
public class SSADestructor extends ModulePass {

    EdgeSplitter edgeSplitter;
    LinkedList<CopyInst> copyInsts;
    LinkedList<CopyInst> simpleCopyList;
    private MXLogger logger;

    public SSADestructor(Module module, MXLogger logger) {
        super(module);
        this.logger = logger;
        this.edgeSplitter = new EdgeSplitter(module, logger);
        this.simpleCopyList = new LinkedList<>();
    }

    private CopyInst getPCopy() {
        for (CopyInst copyInst : copyInsts) {
            if (copyInst.getDest() != copyInst.getSrc()) {
                return copyInst;
            }
        }
        return null;
    }

    private CopyInst getSafeCopy() {
        for (CopyInst copyInst : copyInsts) {
            if (findCircle(copyInst) == null) {
                return copyInst;
            }
        }
        return null;
    }

    private void parallel2sequential(BasicBlock BB) {
        if (BB.getIdentifier().equals("edge_splitter")) return;

        copyInsts = new LinkedList<>(BB.getCopyInsts());
        while (getPCopy() != null) {
            CopyInst safeCopy = getSafeCopy();
            if (safeCopy != null) {
                copyInsts.remove(safeCopy);
                safeCopy.getParent().getInstList().remove(safeCopy);
                safeCopy.isParallel = false;
                simpleCopyList.add(safeCopy);
                continue;
            }
            CopyInst copyInst = getPCopy();
            CopyInst friend = findCircle(copyInst);
            if (friend == null) {
                // this should never happen
                logger.severe("Friend never be null");
            }
            Instruction tmpRegister = new PhiInst(BB, new LocalRegister()); // freshly created vairable
            CopyInst tmpCopy = new CopyInst(BB, tmpRegister, copyInst.getSrc(), false);
            BB.AddInstBeforeBranch(tmpRegister);
            simpleCopyList.add(tmpCopy);
            copyInst.replaceSrc(tmpRegister);
        }
        for (int i = 0; i < simpleCopyList.size(); i++) {
            CopyInst inst = simpleCopyList.get(i);
            if (i == 0 || !inst.toString().equals(simpleCopyList.get(i - 1).toString())) {
                BB.AddInstBeforeBranch(inst);
            }
        }
        simpleCopyList.clear();
    }

    private void parallel2sequential(Function function) {
        for (BasicBlock BB : function.getBlockList()) {
            parallel2sequential(BB);
        }
    }

    private CopyInst findCircle(CopyInst copyInst) {
        for (CopyInst inst : copyInsts) {
            if (inst.getSrc() == copyInst.getDest()) {
                return inst;
            }
        }
        return null;
    }

    @Override
    public boolean optimize() {
        edgeSplitter.optimize();

        for (Function function : TopModule.getFunctionMap().values()) {
            if (!function.isExternal()) parallel2sequential(function);
        }
        return false;
    }
}
