package MxEntity;

import AST.*;
import Frontend.Scope;

import java.util.ArrayList;

public class FunctionEntity extends Entity {

    private Type ReturnType;
    private int ParaListSize;
    private ArrayList<VariableEntity> ParaList;
    private boolean IsMethod;
    private String ClassName;

    public FunctionEntity(String id, Scope father) {
        super(id);
        setScope(father);
        ParaList = new ArrayList<>();
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
        scope.setFuncRetType(ReturnType);
        scope.inFunction = true;
        this.ParaListSize = node.getParaDecList().size();
        ParaList = new ArrayList<>();
        for (ParameterNode para : node.getParaDecList()) {
            VariableEntity mx_var = new VariableEntity(father, para);
            scope.defineVariable(mx_var);
            ParaList.add(mx_var);
        }
    }

    public String getClassName() {
        return ClassName;
    }

    public int getParaListSize() {
        return ParaListSize;
    }

    public ArrayList<VariableEntity> getParaList() {
        return ParaList;
    }

    public Type getReturnType() {
        return ReturnType;
    }

    public boolean isMethod() {
        return IsMethod;
    }
}
