package IR.Instructions;

import IR.BasicBlock;
import IR.Types.IRBaseType;

;

public class CastInst extends Instruction {
    private IRBaseType OriginType, DestType;

    public CastInst(BasicBlock parent, IRBaseType fromType, IRBaseType toType) {
        super(parent, InstType.bitcast);
        this.OriginType = fromType;
        this.DestType = toType;
    }

    public IRBaseType getOriginType() {
        return OriginType;
    }

    public IRBaseType getDestType() {
        return DestType;
    }
}
