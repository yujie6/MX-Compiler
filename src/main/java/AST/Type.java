package AST;

public class Type
{
    private BaseType baseType;
    private String name;
    protected int arrayLevel;

    public Type (Type other) {
        if (other != null) {
            this.baseType = other.baseType;
            this.name = other.name;
            this.arrayLevel = other.arrayLevel;
        }
    }

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
        if (isArray()) {

            return (other.isArray() && arrayLevel == other.arrayLevel && name.equals(other.name))
                    || other.baseType == BaseType.DTYPE_NULL;

        }
        if (isClass()) {
            return name.equals(other.name) || other.baseType == BaseType.DTYPE_NULL;
        }
        return other.baseType == baseType;
    }

    public boolean isClass() {
        return baseType.equals(BaseType.STYPE_CLASS);
    }

    public boolean isNull() {
        return baseType.equals(BaseType.DTYPE_NULL);
    }
    public int getArrayLevel() {
        return arrayLevel;
    }
}
