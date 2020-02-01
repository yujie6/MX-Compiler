package AST;

import Tools.Location;

public class BoolConstNode extends ConstNode {
    Boolean value;
    public BoolConstNode(Location location, boolean value) {
        super(location, new Type(BaseType.DTYPE_BOOL));
        this.value = value;
    }
}
