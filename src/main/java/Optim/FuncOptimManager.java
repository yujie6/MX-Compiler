package Optim;

import IR.Function;
import Optim.FuncAnalysis.*;
import Optim.Transformation.*;

public class FuncOptimManager {
    private Function function;
    private DomTreeBuilder domTreeBuilder;
    private LoopAnalysis LA;
    private AliasAnalysis AA;
    private CFGSimplifier cfgSimplifier;
    private ConstFold constFold;
    private CDGBuilder cdgBuilder;
    private Mem2reg mem2reg;
    private DeadCodeElim dce;
    private AggrDeadCodeElim adce;
    private CommonSubexElim cse;
    private CommonGEPElim cge;
    private LoopICM licm;
    private CondConstPropag sccp;
    private LoadElim loadElim;
    private Peephole peephole;

    public FuncOptimManager(Function func) {
        this.function = func;
        cfgSimplifier = new CFGSimplifier(function);
        domTreeBuilder = new DomTreeBuilder(function);
        func.setDM(domTreeBuilder);
        LA = new LoopAnalysis(function, domTreeBuilder);
        AA = new AliasAnalysis(function, domTreeBuilder);
        mem2reg = new Mem2reg(function, domTreeBuilder);
        dce = new DeadCodeElim(function);
        constFold = new ConstFold(function);
        cdgBuilder = new CDGBuilder(function);
        adce = new AggrDeadCodeElim(function, cdgBuilder);
        cse = new CommonSubexElim(function, domTreeBuilder);
        licm = new LoopICM(function, LA, AA, domTreeBuilder);
        loadElim = new LoadElim(function, AA, domTreeBuilder);
        cge = new CommonGEPElim(function, AA, domTreeBuilder);
        sccp = new CondConstPropag(function);
        peephole = new Peephole(function);
    }

    public String getIdentifier() {
        return function.getIdentifier();
    }

    public void buildControlDependenceGraph() {
        cdgBuilder.build();
    }

    public void adce() {
        cdgBuilder.build();
        adce.optimize();
    }

    public boolean peephole() {
        return peephole.optimize();
    }

    public boolean dce() {
        return dce.optimize();
    }

    public void buildDomTree() {
        domTreeBuilder.build();
    }

    public void loopAnalysis() {
        LA.optimize();
    }

    public boolean mem2reg() {
        return this.mem2reg.optimize();
    }

    public boolean cfgSimplify() {
        return this.cfgSimplifier.optimize();
    }

    public boolean constFold() {
        return this.constFold.optimize();
    }

    public boolean cse() {
        return cse.optimize();
    }

    public boolean cge() {
        return cge.optimize();
    }

    public boolean sccp() {
        return sccp.optimize();
    }

    public boolean licm() {
        return licm.optimize();
    }

    public boolean loadelim() {
        return loadElim.optimize();
    }
}
