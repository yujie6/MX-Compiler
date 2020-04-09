package Optim;

import IR.Function;
import Optim.FuncAnalysis.CDGBuilder;
import Optim.FuncAnalysis.DomTreeBuilder;
import Optim.Transformation.AggrDeadCodeElim;
import Optim.Transformation.DeadCodeElim;
import Optim.Transformation.Mem2reg;

public class FuncOptimManager {
    private Function function;
    private DomTreeBuilder domTreeBuilder;
    private CDGBuilder cdgBuilder;
    private Mem2reg mem2reg;
    private DeadCodeElim dce;
    private AggrDeadCodeElim adce;

    public FuncOptimManager(Function func) {
        this.function = func;
        domTreeBuilder = new DomTreeBuilder(function);
        mem2reg = new Mem2reg(function, domTreeBuilder);
        dce = new DeadCodeElim(function);
        cdgBuilder = new CDGBuilder(function);
        adce = new AggrDeadCodeElim(function, cdgBuilder);
    }

    public void buildControlDependenceGraph() {
        cdgBuilder.build();
    }

    public void adce() {
        adce.optimize();
    }

    public void deadCodeElimination() {
        dce.optimize();
    }

    public void buildDomTree() {
        domTreeBuilder.build();
    }

    public void mem2reg() {
        this.mem2reg.optimize();
    }
}
