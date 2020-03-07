package IR.Instructions;

import BackEnd.IRBuilder;
import IR.BasicBlock;
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
    public GetPtrInst(BasicBlock parent, Value aggregateValue, ArrayList<Value> offsets,
                      IRBaseType elementType) {
        super(parent, InstType.getelementptr);
        this.offsets = offsets;
        this.aggregateValue = aggregateValue;

        if ( aggregateValue.getType() instanceof PointerType ) {
            this.baseAggregateType = ((PointerType) aggregateValue.getType()).getBaseType();
        } else {
            // including global var
            this.baseAggregateType = aggregateValue.getType();
        }
        if (! (baseAggregateType instanceof AggregateType)) {
            IRBuilder.logger.severe("Getptr target is not aggregate type");
            System.exit(1);
        }
        this.type = elementType;

    }

    public GetPtrInst(GetPtrInst array, Value offset, IRBaseType elementType) {
        super(array.getParent(), InstType.getelementptr);
        this.offsets = array.getOffsets();
        this.offsets.add(offset);
        this.aggregateValue = array.aggregateValue;
        this.baseAggregateType = array.baseAggregateType;
        this.type = elementType;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(getRegisterID());
        ans.append(" = getelementptr inbounds ");
        ans.append(baseAggregateType.toString()).append(", ");
        ans.append(aggregateValue.getType());
        if (aggregateValue instanceof GlobalVariable) {
            ans.append("*");
        }
        ans.append(" ");
        ans.append( getRightValueLabel(aggregateValue));
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
}
