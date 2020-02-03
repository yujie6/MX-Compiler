package Tools;

public class MXError extends Error {

    public MXError(String msg)
    {
        super(String.format("Compiler ERROR with msg: %s", msg));
    }
}
