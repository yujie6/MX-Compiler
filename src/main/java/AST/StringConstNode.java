package AST;

import Tools.Location;

public class StringConstNode extends ConstNode {
    String value;
    public StringConstNode(Location location, String value) {
        super(location, new Type(BaseType.DTYPE_STRING));
        this.value = value;
    }
}
