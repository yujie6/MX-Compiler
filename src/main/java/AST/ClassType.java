package AST;

import Tools.Location;

public class ClassType extends Type {
    public ClassType(String id) {
        super(BaseType.STYPE_CLASS);
        setName(id);
        this.arrayLevel = 0;
    }
}
