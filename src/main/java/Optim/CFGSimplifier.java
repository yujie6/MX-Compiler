package Optim;

import IR.Argument;
import IR.BasicBlock;
import IR.Function;
import IR.IRVisitor;
import IR.Module;

public class CFGSimplifier implements IRVisitor {
    @Override
    public Object visit(BasicBlock node) {
        return null;
    }

    @Override
    public Object visit(Argument node) {
        return null;
    }

    @Override
    public Object visit(Function node) {
        return null;
    }

    @Override
    public Object visit(Module node) {
        return null;
    }
}
