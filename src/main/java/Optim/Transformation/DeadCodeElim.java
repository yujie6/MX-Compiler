package Optim.Transformation;

import IR.Function;
import Optim.Pass;

/**
 * This file implements dead inst elimination and dead code elimination.
 *
 * Dead Inst Elimination performs a single pass over the function removing
 * instructions that are obviously dead.  Dead Code Elimination is similar, but
 * it rechecks instructions that were used by removed instructions to see if
 * they are newly dead.
 */
public class DeadCodeElim extends Pass {

    private Function function;
    public DeadCodeElim(Function func) {
        this.function = func;
    }

    @Override
    public boolean optimize() {
        return false;
    }
}
