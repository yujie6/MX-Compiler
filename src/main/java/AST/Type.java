package AST;

public class Type
{
    BaseType baseType;

    public Type(BaseType type) { this.baseType = type; }

    public BaseType getBaseType()
    {
        return baseType;
    }
}
