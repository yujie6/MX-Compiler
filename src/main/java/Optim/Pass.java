package Optim;

import IR.BasicBlock;
import IR.Function;
import IR.IRVisitor;

public abstract class Pass {

    abstract boolean optimize();
}
