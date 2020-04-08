package Optim;

import IR.Function;
import Optim.FuncAnalysis.DomTreeBuilder;
import Optim.Transformation.Mem2reg;

public class FuncOptimManager {
    private Function function;
    private DomTreeBuilder domTreeBuilder;
    private Mem2reg mem2reg;

    public FuncOptimManager(Function func) {
        this.function = func;
        domTreeBuilder = new DomTreeBuilder(function);
        mem2reg = new Mem2reg(function, domTreeBuilder);
    }

    public void buildDomTree() {
        domTreeBuilder.build();
    }

    public void mem2reg() {
        this.mem2reg.optimize();
    }
}
