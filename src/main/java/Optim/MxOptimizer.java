package Optim;

import IR.Function;
import IR.Module;
import Optim.Transformation.DeadFuncElim;
import Tools.MXLogger;

import java.util.ArrayList;

public class MxOptimizer {
    private Module TopModule;
    public static MXLogger logger;


    private ArrayList<FuncOptimManager> funcOptimizers;
    public MxOptimizer(Module topModule, MXLogger logger) {
        this.TopModule = topModule;
        MxOptimizer.logger = logger;
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

    private void buildControlDependenceGraph() {
        for (FuncOptimManager optimizer : funcOptimizers) {
            optimizer.buildControlDependenceGraph();;
        }
    }

    private void mem2reg() {
        for (FuncOptimManager optimizer : funcOptimizers) {
            optimizer.mem2reg();
        }
    }

    private void deadCodeElimination () {
        for (FuncOptimManager optimManager : funcOptimizers) {
            optimManager.dce();
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

    private void cfgSimplify() {
        for (FuncOptimManager optimManager : funcOptimizers) {
            optimManager.cfgSimplify();
        }
    }

    public void optimize() {
        (new DeadFuncElim(TopModule)).optimize();
        buildDomTrees();
        mem2reg();
        // deadCodeElimination();
        // aggressiveDeadCodeElimination();
        // commonSubexpressionElimination();
    }
}
