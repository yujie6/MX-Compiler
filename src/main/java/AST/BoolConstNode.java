package AST;

import Tools.Location;

public class BoolConstNode extends ConstNode {
    private Boolean value;
    public BoolConstNode(Location location, boolean value) {
        super(location, new Type(BaseType.DTYPE_BOOL));
        this.value = value;
    }

    public Boolean getValue() {
        return value;
    }
}
