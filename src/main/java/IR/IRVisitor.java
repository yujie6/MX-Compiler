package IR;

public interface IRVisitor<T> {
    T visitModule(Module module);
}
