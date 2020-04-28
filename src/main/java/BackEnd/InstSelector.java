package BackEnd;


import IR.BasicBlock;
import IR.Function;
import IR.IRVisitor;
import IR.Module;
import Target.RVBlock;
import Target.RVFunction;
import Target.RVModule;
import Tools.MXLogger;

/**
 * Maximal munch algorithm:
 */
public class InstSelector implements IRVisitor {

    private RVModule riscvTopModule;
    private Module IRModule;
    private MXLogger logger;


    public InstSelector(Module IRModule, MXLogger logger) {
        this.riscvTopModule = new RVModule();
        this.IRModule = IRModule;
        this.logger = logger;
        for (Function function : IRModule.getFunctionMap().values()) {
            RVFunction rvFunction = (RVFunction) visit(function);
            this.riscvTopModule.addFunction(rvFunction);
        }
    }

    public RVModule getRISCVTopModule() {
        return riscvTopModule;
    }

    @Override
    public Object visit(BasicBlock node) {
        RVBlock rvBlock = new RVBlock(node);



        return rvBlock;
    }

    @Override
    public Object visit(Function node) {
        RVFunction rvFunction = new RVFunction(node);
        for (BasicBlock BB : node.getBlockList()) {
            RVBlock rvBlock = (RVBlock) visit(BB);
            rvFunction.addRVBlock(rvBlock);
        }
        return rvFunction;
    }
}
