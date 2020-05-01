package BackEnd;

import Target.AsmVisitor;
import Target.RVBlock;
import Target.RVFunction;
import Target.RVModule;

public class RegAllocator extends RVPass implements AsmVisitor<Object> {
    public RegAllocator(RVModule topModule) {
        super(topModule);
    }

    @Override
    public void run() {

    }


    @Override
    public Object visit(RVModule rvModule) {
        return null;
    }

    @Override
    public Object visit(RVFunction rvFunction) {
        return null;
    }

    @Override
    public Object visit(RVBlock rvBlock) {
        return null;
    }
}
