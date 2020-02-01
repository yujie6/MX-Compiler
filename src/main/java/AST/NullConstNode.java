package AST;

import Tools.Location;

public class NullConstNode extends ConstNode {
    public NullConstNode(Location location) {
        super(location, new Type(BaseType.DTYPE_NULL));
    }
}
