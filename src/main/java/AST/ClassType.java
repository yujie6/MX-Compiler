package AST;

import Tools.Location;

public class ClassType extends Type {
    String ClassIdentifier;
    public ClassType(String id) {
        super(BaseType.STYPE_CLASS);
        this.ClassIdentifier = id;
    }
}
