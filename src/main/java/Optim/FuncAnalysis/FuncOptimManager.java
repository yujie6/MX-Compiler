package Optim.FuncAnalysis;

import IR.Function;

public class FuncOptimManager {
    private Function function;
    private DomTreeBuilder domTreeBuilder;

    public FuncOptimManager(Function func) {
        this.function = func;
        domTreeBuilder = new DomTreeBuilder(function);
    }

    public void buildDomTree() {
        domTreeBuilder.build();
    }
}
