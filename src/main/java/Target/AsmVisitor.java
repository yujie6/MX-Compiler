package Target;

public interface AsmVisitor<T> {
    T visit(RVModule rvModule);
    T visit(RVFunction rvFunction);
    T visit(RVBlock rvBlock);
}
