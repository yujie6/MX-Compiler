package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;
import IR.Use;
import IR.Value;
import Tools.Operators;

public class CmpInst extends Instruction {

    Operators op;

    public CmpInst(BasicBlock parent, IRBaseType type, Operators bop, Value LHS, Value RHS) {
        super(parent, InstType.Cmp);
        this.UseList.add(new Use(LHS, this));
        this.UseList.add(new Use(RHS, this));
        this.op = bop;
        this.type = type;
    }


}
