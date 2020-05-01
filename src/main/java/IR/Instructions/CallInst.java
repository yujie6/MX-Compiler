package IR.Instructions;

import IR.*;

import java.util.ArrayList;

public class CallInst extends Instruction {

    private ArrayList<Value> ArgumentList;


    public CallInst(BasicBlock parent, Function function, ArrayList<Value> paras) {
        super(parent, InstType.call);
        this.UseList.add(new Use(function, this));
        this.ArgumentList = paras;
        this.type = function.getFunctionType().getReturnType();
        for (Value para : paras) {
            this.UseList.add(new Use(para, this));
        }
    }

    public boolean isVoid() {
        return this.type.toString().equals("void");
    }

    public Function getCalledFunction() {
        return (Function) this.UseList.get(0).getVal();
    }

    public ArrayList<Value> getArgumentList() {
        return ArgumentList;
    }

    public Value getArgument(int index) {
        return this.UseList.get(index + 1).getVal();
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        if (isVoid()) {
            ans.append("call void @");
        } else {
            ans.append( RegisterID).append(" = call ").append(this.type.toString()).append(" @");
        }
        ans.append(getCalledFunction().getIdentifier()).append("(");
        if (!ArgumentList.isEmpty()) {
            Value var = getArgument(0);
            ans.append(var.getType().toString()).append(" ").append(getRightValueLabel(var));
            for (int i = 1; i < ArgumentList.size(); i++) {
                var = getArgument(i);
                ans.append(", ").append(var.getType().toString()).append(" ").append(getRightValueLabel(var));
            }
        }
        ans.append(")\n");
        return ans.toString();
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
