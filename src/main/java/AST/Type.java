package AST;

public class Type
{
    private BaseType baseType;
    private String name;
    public int arrayLevel;

    public Type(BaseType type) {
        this.baseType = type;
        this.name = type.toString();
        this.arrayLevel = 0;
    }

    public Type(BaseType type, int arrayLevel, String name) {
        // assert (type == BaseType.STYPE_ARRAY);
        this.baseType = type;
        this.name = name + String.format("'s %d-dim-Array-Type", arrayLevel);
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
}
