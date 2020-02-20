package IR;

public interface IRVisitor<T> {
    T visit(BasicBlock node);
    T visit(Argument node);

    T visit(Function node);

    T visit(Module node);
}
