package AST;

import Tools.Location;

public class DoubleConstNode extends ConstNode {
    double value;
    public DoubleConstNode(Location location, Type type, double value) {
        super(location, type);
        this.value = value;
    }
}
