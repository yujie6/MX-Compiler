package Optim;

import IR.BasicBlock;
import IR.Function;
import IR.IRVisitor;

public abstract class Pass implements IRVisitor {



    abstract boolean optimize();
}
