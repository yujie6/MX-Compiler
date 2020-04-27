package Optim;

import IR.Function;

public abstract class FunctionPass extends Pass {

    protected Function function;

    public FunctionPass(Function function) {
        this.function = function;
    }
}
