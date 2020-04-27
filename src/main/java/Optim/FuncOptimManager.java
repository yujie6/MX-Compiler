package Optim;

import IR.Function;
import Optim.FuncAnalysis.CDGBuilder;
import Optim.FuncAnalysis.DomTreeBuilder;
import Optim.FuncAnalysis.Loop;
import Optim.FuncAnalysis.LoopAnalysis;
import Optim.Transformation.*;

public class FuncOptimManager {
    private Function function;
    private DomTreeBuilder domTreeBuilder;
    private LoopAnalysis LA;
    private CFGSimplifier cfgSimplifier;

    private CDGBuilder cdgBuilder;
    private Mem2reg mem2reg;
    private DeadCodeElim dce;
    private AggrDeadCodeElim adce;
    private CommonSubexElim cse;
    private LoopICM licm;

    public FuncOptimManager(Function func) {
        this.function = func;
        cfgSimplifier = new CFGSimplifier(function);
        domTreeBuilder = new DomTreeBuilder(function);
        LA = new LoopAnalysis(function, domTreeBuilder);
        mem2reg = new Mem2reg(function, domTreeBuilder);
        dce = new DeadCodeElim(function);
        cdgBuilder = new CDGBuilder(function);
        adce = new AggrDeadCodeElim(function, cdgBuilder);
        cse = new CommonSubexElim(function, domTreeBuilder);
        licm = new LoopICM(function, LA);
    }

    public void buildControlDependenceGraph() {
        cdgBuilder.build();
    }

    public void adce() {
        cdgBuilder.build();
        adce.optimize();
    }

    public void dce() {
        dce.optimize();
    }

    public void buildDomTree() {
        domTreeBuilder.build();
    }

    public void buildLoopTree() {
        LA.optimize();
    }

    public void mem2reg() {
        this.mem2reg.optimize();
    }

    public void cfgSimplify() {
        this.cfgSimplifier.optimize();
    }

    public void cse() {
        cse.optimize();
    }

    public void licm() {
        licm.optimize();
    }
}
