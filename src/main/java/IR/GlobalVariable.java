package IR;

import IR.Constants.Constant;
import IR.Constants.StringConst;
import IR.Types.IRBaseType;
import IR.Types.PointerType;

public class GlobalVariable extends Value {

    private String Identifier;
    private Value InitValue;
    private IRBaseType originalType;
    static int globalStringNum = 0;
    public boolean isStringConst;

    public GlobalVariable(IRBaseType type, String id, Value initValue) {
        super(ValueType.GLOBAL_VAR);
        this.originalType = type;
        this.type = new PointerType(type);
        this.isStringConst = false;
        if (id == null && type.equals(Module.STRING)) {
            this.isStringConst = true;
            this.Identifier = ".str." + globalStringNum;
            globalStringNum += 1;
        } else this.Identifier = id;
        this.InitValue = initValue;
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        // this func shall never be used
        return null;
    }

    @Override
    public String toString() {
        // TODO Global variable should have pointer reference, including global string
        StringBuilder ans = new StringBuilder("@");
        ans.append(Identifier).append(" = ");
        if (isStringConst) {
            ans.append("private unnamed_addr constant ");
            ans.append("[").append( ((StringConst) getInitValue() ).getStrSize() );
            ans.append(" x ").append("i8] ");
            ans.append( getInitValue().toString() );
        } else {
            ans.append("dso_local global ").append(this.originalType.toString());
            ans.append(" ");
            if (InitValue instanceof Constant) {
                ans.append(InitValue.toString()); // init value shall be const or new expr
            } else {
                ans.append("zeroinitializer");
            }
        }

        ans.append(" , align ");
        int align = (type instanceof PointerType) ? 8 : 4;
        ans.append(String.valueOf(align)).append("\n");


        return ans.toString();
    }

    public IRBaseType getOriginalType() {
        return originalType;
    }

    public void setOriginalType(IRBaseType originalType) {
        this.originalType = originalType;
    }

    public String getIdentifier() {
        return Identifier;
    }

    public void setInitValue(Value initValue) {
        InitValue = initValue;
    }

    public Value getInitValue() {
        return InitValue;
    }
}
