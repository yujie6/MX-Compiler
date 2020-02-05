package Tools;

public class MXError extends Error {

    public MXError(String message, Location location)
    {
        super(String.format("Compiler ERROR at %d:%d with msg: %s",
                location.getLine(),
                location.getColumn(),
                message
        ));
    }

    public MXError(String message)
    {
        super(String.format("Compiler ERROR with msg: %s", message));
    }
}
