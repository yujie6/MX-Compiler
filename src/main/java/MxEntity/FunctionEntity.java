package MxEntity;

import AST.*;
import Frontend.Scope;

public class FunctionEntity extends Entity {

    private Type ReturnType;
    private int ParaListSize;
    private boolean IsMethod;
    private String ClassName;

    public FunctionEntity(String id, Scope father) {
        super(id);
        setScope(father);
    }

    public FunctionEntity(Scope father, FunctionDecNode node, boolean isMethod, String className) {
        super(node.getIdentifier());
        setScope(father);
        this.IsMethod = isMethod;
        this.ClassName = (isMethod) ? className : null;
        if (node instanceof MethodDecNode) {
            if (((MethodDecNode) node).isConstructMethod()) {
                ReturnType = new ClassType(ClassName);
            }
        }
        ReturnType = node.getReturnType().getType();
        this.ParaListSize = node.getParaDecList().size();
        for (ParameterNode para : node.getParaDecList()) {
            VariableEntity mx_var = new VariableEntity(para);
            scope.defineVariable(mx_var);
        }
    }

    public int getParaListSize() {
        return ParaListSize;
    }

    public Type getReturnType() {
        return ReturnType;
    }

    public boolean isMethod() {
        return IsMethod;
    }
}
