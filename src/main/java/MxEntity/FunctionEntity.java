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

    public FunctionEntity(String id, Type RetType, String className,
                          boolean isMethod, Scope father,
                          ArrayList<VariableEntity> Paras) {
        super(id);
        setScope(father);
        this.ParaList = Paras;
        this.IsMethod = isMethod;
        this.ReturnType = RetType;
        this.ClassName = className;
    }

    public FunctionEntity(Scope father, FunctionDecNode node, boolean isMethod, String className) {
        super(node.getIdentifier());
        this.scope = new Scope(father);
        this.IsMethod = isMethod;
        if (isMethod) {
            this.ClassName = className;
            // setIdentifier(className + '.' + node.getIdentifier());
        } else this.ClassName = null;
        if (node instanceof MethodDecNode) {
            if (((MethodDecNode) node).isConstructMethod()) {
                ReturnType = new ClassType(ClassName);
            }
        }
        ReturnType = node.getReturnType().getType();
        this.scope.setFuncRetType(ReturnType);
        this.scope.inFunction = true;
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
