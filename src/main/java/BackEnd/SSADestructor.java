package BackEnd;

import IR.Function;
import IR.Module;
import Optim.ModulePass;
import Optim.Pass;
import Tools.MXLogger;

public class SSADestructor extends ModulePass {

    Module TopModule;
    private MXLogger logger;
    public SSADestructor(Module module, MXLogger logger) {
        super(module);
        this.logger = logger;
    }

    public void destruct() {
        for (Function function : TopModule.getFunctionMap().values()) {
            SSADestruct(function);
        }
    }

    private void SSADestruct (Function function) {

    }

    @Override
    public boolean optimize() {
        return false;
    }
}
