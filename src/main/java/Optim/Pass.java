package Optim;

import IR.BasicBlock;
import IR.Function;
import IR.IRVisitor;

public abstract class Pass {

    public abstract boolean optimize();
}
