package MxEntity;

import AST.FunctionDecNode;
import AST.ParameterNode;
import AST.Type;
import Frontend.Scope;

public class FunctionEntity extends Entity {

    private Type ReturnType;
    private int ParaListSize;

    public FunctionEntity(String id, Scope father) {
        super(id);
        setScope(father);
    }

    public FunctionEntity(Scope father, FunctionDecNode node) {
        super(node.getIdentifier());
        setScope(father);
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
}
