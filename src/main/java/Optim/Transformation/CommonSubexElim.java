package Optim.Transformation;

import IR.Function;
import Optim.FuncOptimManager;
import Optim.Pass;

public class CommonSubexElim extends Pass {

    private Function function;

    public CommonSubexElim(Function function1) {
        this.function = function1;
    }

    @Override
    public boolean optimize() {
        return false;
    }
}
