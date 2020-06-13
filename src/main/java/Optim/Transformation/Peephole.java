package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.Instruction;
import Optim.FunctionPass;

public class Peephole extends FunctionPass {

    private int elimNum = 0;

    public Peephole(Function function) {
        super(function);
    }

    @Override
    public boolean optimize() {
        elimNum = 0;
        for (BasicBlock BB : function.getBlockList()) {
            for (int i = 1; i < BB.getInstList().size(); i++) {
                Instruction inst = BB.getInstList().get(i);
                Instruction prev = BB.getInstList().get(i-1);

            }
        }
        return elimNum != 0;
    }
}
