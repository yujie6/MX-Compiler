package AST;

public class VoidType extends Type {
    public VoidType() {
        baseType = BaseType.RTYPE_VOID;
    }

    @Override
    public boolean equals(Object obj) {
        return obj.getClass().equals(VoidType.class);
    }
}
