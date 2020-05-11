package IR.Instructions;

import BackEnd.IRBuilder;
import IR.*;
import IR.Constants.IntConst;
import IR.Constants.StringConst;
import IR.Types.AggregateType;
import IR.Types.IRBaseType;
import IR.Types.PointerType;

import java.util.ArrayList;

public class GetPtrInst extends Instruction {
    private IRBaseType baseAggregateType;
    private IRBaseType elementType;

    public GetPtrInst(BasicBlock parent, Value aggregateValue, ArrayList<Value> offsets,
                      IRBaseType elementType) {
        super(parent, InstType.getelementptr);
        this.UseList.add(new Use(aggregateValue, this));
        for (Value offset : offsets) {
            this.UseList.add(new Use(offset, this));
        }


        if (aggregateValue.getType() instanceof PointerType) {
            this.baseAggregateType = ((PointerType) aggregateValue.getType()).getBaseType();
        } else {
            // including global var
            this.baseAggregateType = aggregateValue.getType();
        }
        this.elementType = elementType;
        this.type = new PointerType(elementType);
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getRegisterID());
        if (getAggregateValue() instanceof GlobalVariable) {
            // global string const
            if (((GlobalVariable) getAggregateValue()).getInitValue() instanceof StringConst) {
                GlobalVariable gvar = (GlobalVariable) getAggregateValue();
                ans.append(" = getelementptr inbounds ");

                ans.append("[").append(((StringConst) gvar.getInitValue()).getStrSize());
                ans.append(" x ").append("i8], ");

                ans.append("[").append(((StringConst) gvar.getInitValue()).getStrSize());
                ans.append(" x ").append("i8]* @").append(gvar.getIdentifier());
                for (Value off_t : getOffsets()) {
                    ans.append(", ").append(off_t.getType().toString()).append(" ");
                    ans.append(getRightValueLabel(off_t));
                }
                ans.append("\n");
                return ans.toString();
            }
        }

        // normal situations

        ans.append(" = getelementptr inbounds ");
        ans.append(baseAggregateType.toString()).append(", ");
        ans.append(getAggregateValue().getType());
        ans.append(" ");
        ans.append(getRightValueLabel(getAggregateValue()));
        for (Value off_t : getOffsets()) {
            ans.append(", ").append(off_t.getType().toString()).append(" ");
            ans.append(getRightValueLabel(off_t));
        }

        ans.append("\n");
        return ans.toString();
    }

    public boolean hasAllZeroOffsets() {
        for (Value var : getOffsets()) {
            if (var instanceof IntConst) {
                if ( ((IntConst) var).ConstValue != 0) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public Value getAggregateValue() {
        return this.UseList.get(0).getVal();
    }

    public ArrayList<Value> getOffsets() {
        ArrayList<Value> offsets = new ArrayList<>();
        for (int i = 1; i < this.UseList.size(); i++) {
            offsets.add(this.UseList.get(i).getVal());
        }
        return offsets;
    }

    public int getTotalOffset() {
        int s = 0;
        for (Value var : getOffsets()) {
            if (var instanceof IntConst) {
                s += ((IntConst) var).ConstValue;
            } else return -42;
        }
        return s;
    }

    public IRBaseType getBaseAggregateType() {
        return baseAggregateType;
    }

    public IRBaseType getElementType() {
        return elementType;
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }
}
