package AST;

import Tools.Location;

public class IntConstNode extends ConstNode {
    private int value;
    public IntConstNode(Location location, int value) {
        super(location, new Type(BaseType.DTYPE_INT));
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
