package IR.Instructions;

import BackEnd.IRBuilder;
import IR.BasicBlock;
import IR.Constants.IntConst;
import IR.Constants.StringConst;
import IR.GlobalVariable;
import IR.Types.AggregateType;
import IR.Types.IRBaseType;
import IR.Types.PointerType;
import IR.Value;

import java.util.ArrayList;

public class GetPtrInst extends Instruction {
    private ArrayList<Value> offsets;
    private Value aggregateValue;
    private IRBaseType baseAggregateType;
    private IRBaseType elementType;

    public GetPtrInst(BasicBlock parent, Value aggregateValue, ArrayList<Value> offsets,
                      IRBaseType elementType) {
        super(parent, InstType.getelementptr);
        this.offsets = offsets;
        this.aggregateValue = aggregateValue;

        if (aggregateValue.getType() instanceof PointerType) {
            this.baseAggregateType = ((PointerType) aggregateValue.getType()).getBaseType();
        } else {
            // including global var
            this.baseAggregateType = aggregateValue.getType();
        }
        this.elementType = elementType;
        this.type = new PointerType(elementType);
    }

    public GetPtrInst(GetPtrInst array, Value offset, IRBaseType elementType) {
        super(array.getParent(), InstType.getelementptr);
        this.offsets = array.getOffsets();
        this.offsets.add(offset);
        this.aggregateValue = array.aggregateValue;
        this.baseAggregateType = array.baseAggregateType;
        this.type = elementType;
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getRegisterID());
        if (aggregateValue instanceof GlobalVariable) {
            // global string const
            if (((GlobalVariable) aggregateValue).getInitValue() instanceof StringConst) {
                GlobalVariable gvar = (GlobalVariable) aggregateValue;
                ans.append(" = getelementptr inbounds ");

                ans.append("[").append(((StringConst) gvar.getInitValue()).getStrSize());
                ans.append(" x ").append("i8], ");

                ans.append("[").append(((StringConst) gvar.getInitValue()).getStrSize());
                ans.append(" x ").append("i8]* @").append(gvar.getIdentifier());
                for (Value off_t : offsets) {
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
        ans.append(aggregateValue.getType());
        ans.append(" ");
        ans.append(getRightValueLabel(aggregateValue));
        for (Value off_t : offsets) {
            ans.append(", ").append(off_t.getType().toString()).append(" ");
            ans.append(getRightValueLabel(off_t));
        }

        ans.append("\n");
        return ans.toString();
    }

    public Value getAggregateValue() {
        return aggregateValue;
    }

    public ArrayList<Value> getOffsets() {
        return offsets;
    }

    public IRBaseType getBaseAggregateType() {
        return baseAggregateType;
    }

    public IRBaseType getElementType() {
        return elementType;
    }
}
