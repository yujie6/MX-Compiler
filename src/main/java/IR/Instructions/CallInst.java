package IR.Instructions;

import IR.*;

import java.util.ArrayList;

public class CallInst extends Instruction {

    public CallInst(BasicBlock parent, Function function, ArrayList<Value> paras) {
        super(parent, InstType.call);
        this.UseList.add(new Use(function, this));
        this.type = function.getFunctionType().getReturnType();
        for (Value para : paras) {
            this.UseList.add(new Use(para, this));
        }
    }

    public boolean isVoid() {
        return this.type.toString().equals("void");
    }

    public Function getCallee() {
        return (Function) this.UseList.get(0).getVal();
    }

    public ArrayList<Value> getArgumentList() {
        ArrayList<Value> args = new ArrayList<>();
        for (int i = 0; i < getCallee().getParameterList().size(); i++) {
            args.add(getArgument(i));
        }
        return args;
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
        ans.append(getCallee().getIdentifier()).append("(");
        ArrayList<Value> argList = getArgumentList();
        if (!argList.isEmpty()) {
            Value var = argList.get(0);
            ans.append(var.getType().toString()).append(" ").append(getRightValueLabel(var));
            for (int i = 1; i < argList.size(); i++) {
                var = argList.get(i);
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
