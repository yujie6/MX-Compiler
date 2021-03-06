package Optim;

import IR.Function;
import IR.Module;
import Optim.Transformation.DeadFuncElim;
import Optim.Transformation.FunctionInline;
import Optim.Transformation.Global2Local;
import Tools.MXLogger;

import java.util.ArrayList;

public class MxOptimizer {
    private Module TopModule;
    public static MXLogger logger;
    public FunctionInline functionInline;

    private ArrayList<FuncOptimManager> funcOptimizers;
    public MxOptimizer(Module topModule, MXLogger logger) {
        this.TopModule = topModule;
        MxOptimizer.logger = logger;
        functionInline = new FunctionInline(this.TopModule);
        funcOptimizers = new ArrayList<>();
        for (Function function : TopModule.getFunctionMap().values()) {
            if (!function.isExternal())
                funcOptimizers.add(new FuncOptimManager(function));
        }
    }

    private void global2local() {
        (new Global2Local(TopModule) ).optimize();
    }

    private void buildDomTrees() {
        for (FuncOptimManager optimizer : funcOptimizers) {
            optimizer.cfgSimplify();
            optimizer.buildDomTree();
        }
    }

    private void mem2reg() {
        for (FuncOptimManager optimizer : funcOptimizers) {
            optimizer.mem2reg();
        }
    }

    private void aggressiveDeadCodeElimination() {
        for (FuncOptimManager optimManager : funcOptimizers) {
            optimManager.adce();
        }
    }

    private void commonSubexpressionElimination() {
        for (FuncOptimManager optimManager : funcOptimizers) {
            optimManager.cse();
        }
    }

    private void commonGetElementPtrElimination() {
        for (FuncOptimManager optimManager : funcOptimizers) {
            optimManager.cge();
        }
    }

    private void cfgSimplify() {
        for (FuncOptimManager optimManager : funcOptimizers) {
            optimManager.cfgSimplify();
        }
    }

    private void loopAnalysis() {
        for (FuncOptimManager optimManager : funcOptimizers) {
            optimManager.loopAnalysis();
        }
    }

    private void loopInvariantCodeMotion() {
        for (FuncOptimManager optimManager : funcOptimizers) {
            optimManager.licm();
        }
    }

    private void redundantLoadElimination() {
        for (FuncOptimManager optimManager : funcOptimizers) {
            optimManager.loadelim();
        }
    }

    private void localOptimize() {
        for (FuncOptimManager optimManager : funcOptimizers) {
            if (!TopModule.getFunctionMap().containsKey(optimManager.getIdentifier())) {
                continue;
            }
            optimManager.buildDomTree();
            while (true) {
                boolean changed = false;
                changed = optimManager.constFold();
                changed |= optimManager.dce();
                changed |= optimManager.cse();
                if (instNum < 3000)
                    changed |= optimManager.loadelim();
                // changed |= optimManager.sccp();
                changed |= optimManager.cge();
                changed |= optimManager.peephole();
                changed |= optimManager.cfgSimplify();
                if (!changed) break;
            }
        }
    }

    private void LICM() {
        loopAnalysis();
        for (FuncOptimManager optimizer : funcOptimizers) {
            optimizer.buildDomTree();
        }
        loopInvariantCodeMotion();
        cfgSimplify();
    }

    private int instNum;


    public void optimize() {
        (new DeadFuncElim(TopModule)).optimize();
        buildDomTrees();
        global2local();
        mem2reg();
        instNum = TopModule.getInstNum();
        do {
            localOptimize();
        } while (functionInline.optimize());

        LICM();
        localOptimize();
    }
}
