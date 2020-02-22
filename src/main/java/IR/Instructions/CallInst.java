package IR.Instructions;

import IR.BasicBlock;
import IR.Function;
import IR.Use;
import IR.Value;

import java.util.ArrayList;

public class CallInst extends Instruction {

    private ArrayList<Value> ArgumentList;


    public CallInst(BasicBlock parent, Function function, ArrayList<Value> paras) {
        super(parent, InstType.call);
        this.UseList.add(new Use(function, this));;
        this.ArgumentList = paras;
        this.type = function.getFunctionType().getReturnType();
        for (Value para : paras) {
            this.UseList.add(new Use(para, this));
        }
    }

    public Function getCalledFunction() {
        return (Function) this.UseList.get(0).getVal();
    }

    public ArrayList<Value> getArgumentList() {
        return ArgumentList;
    }
}
