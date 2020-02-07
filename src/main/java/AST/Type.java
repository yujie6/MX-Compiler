package AST;

public class Type
{
    private BaseType baseType;
    private String name;
    protected int arrayLevel;

    public Type(BaseType type) {
        this.baseType = type;
        this.name = type.toString();
        this.arrayLevel = 0;
    }

    public Type(BaseType type, int arrayLevel, String name) {
        // assert (type == BaseType.STYPE_ARRAY);
        this.baseType = type;
        this.name = name; // + String.format("'s %d-dim-Array-Type", arrayLevel);
        this.arrayLevel = arrayLevel;
    }

    public BaseType getBaseType()
    {
        return baseType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isBool() {
        return (baseType.equals(BaseType.DTYPE_BOOL));
    }

    public boolean isString() {
        return baseType.equals(BaseType.DTYPE_STRING);
    }

    public boolean isInt() {
        return (baseType.equals(BaseType.DTYPE_INT));
    }

    public boolean isArray() {
        return arrayLevel > 0;
    }

    public boolean equals(Type other) {
        if (other.baseType != baseType) return false;
        if (baseType == BaseType.STYPE_CLASS) {
            return name.equals(other.name);
        }
        return true;
    }

    public boolean isClass() {
        return baseType.equals(BaseType.STYPE_CLASS);
    }

    public int getArrayLevel() {
        return arrayLevel;
    }
}
