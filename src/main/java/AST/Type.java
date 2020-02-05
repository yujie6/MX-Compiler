package AST;

public class Type
{
    private BaseType baseType;
    private String name;

    public Type(BaseType type) {
        this.baseType = type;
        this.name = type.toString();
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
