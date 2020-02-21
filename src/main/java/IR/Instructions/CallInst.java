package IR.Instructions;

import IR.BasicBlock;
import IR.Function;
import IR.Value;

import java.util.ArrayList;

public class CallInst extends Instruction {

    private Function CalledFunction;
    private ArrayList<Value> ArgumentList;


    public CallInst(BasicBlock parent, Function function, ArrayList<Value> paras) {
        super(parent);
        this.CalledFunction = function;
        this.ArgumentList = paras;
    }

    public Function getCalledFunction() {
        return CalledFunction;
    }

    public ArrayList<Value> getArgumentList() {
        return ArgumentList;
    }
}
