package Optim;

import IR.BasicBlock;
import IR.Function;

/**
 * This file exposes an interface to promote alloca instructions to SSA
 * registers, by using the SSA construction algorithm.
 */
public class Mem2reg  extends Pass {
    @Override
    boolean optimize() {
        return false;
    }

    @Override
    public Object visit(BasicBlock node) {
        return null;
    }

    @Override
    public Object visit(Function node) {
        return null;
    }
}
