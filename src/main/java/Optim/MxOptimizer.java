package Optim;

import IR.Function;
import IR.Module;
import Optim.FuncAnalysis.DomTreeBuilder;
import Optim.FuncAnalysis.FuncOptimManager;
import Tools.MXLogger;

import java.util.ArrayList;

public class MxOptimizer {
    private Module TopModule;
    public static MXLogger logger;


    private ArrayList<FuncOptimManager> funcOptimizers;
    public MxOptimizer(Module topModule, MXLogger logger) {
        this.TopModule = topModule;
        this.logger = logger;
        funcOptimizers = new ArrayList<>();
        for (Function function : TopModule.getFunctionMap().values()) {
            if (!function.isExternal())
                funcOptimizers.add(new FuncOptimManager(function));
        }
    }

    private void buildDomTrees() {
        for (FuncOptimManager optimizer : funcOptimizers) {
            optimizer.buildDomTree();
        }
    }

    public void optimize() {
        buildDomTrees();

    }
}
