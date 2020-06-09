package Optim;

import IR.Function;
import IR.Module;
import Optim.Transformation.DeadFuncElim;
import Optim.Transformation.FunctionInline;
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


    public void optimize() {
        (new DeadFuncElim(TopModule)).optimize();
        buildDomTrees();
        mem2reg();
        do {
            for (FuncOptimManager optimManager : funcOptimizers) {
                optimManager.buildDomTree();
                while (true) {
                    boolean changed = false;
                    changed = optimManager.constFold();
                    changed |= optimManager.dce();
                    changed |= optimManager.cse();
                    // changed |= optimManager.sccp();
                    //changed |= optimManager.cfgSimplify();
                    if (!changed) break;
                }
            }
        } while (functionInline.optimize());
        /*loopAnalysis();
        buildDomTrees();
        loopInvariantCodeMotion();*/

    }
}
