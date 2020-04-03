package IR;

public interface IRVisitor<T> {
    T visit(BasicBlock node);
    T visit(Function node);
}
