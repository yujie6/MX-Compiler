package MxEntity;

import AST.*;
import Frontend.GlobalScopeBuilder;
import Frontend.Scope;
import Frontend.SemanticChecker;

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
        } else this.ClassName = null;
        if (node instanceof MethodDecNode) {
            if (((MethodDecNode) node).isConstructMethod()) {
                if (!node.getIdentifier().equals(ClassName) ) {
                    GlobalScopeBuilder.logger.severe("Construct method should has the same name as class name.",
                            node.GetLocation());
                }
                ReturnType = new ClassType(ClassName);
            }
            else {
                if (node.getIdentifier().equals(ClassName)) {
                    GlobalScopeBuilder.logger.severe("Constructor type error.", node.GetLocation());
                }
                ReturnType = node.getReturnType().getType();
            }
        }
        else ReturnType = node.getReturnType().getType();

        this.scope.setFuncRetType(ReturnType);
        this.scope.inFunction = true;
        this.ParaListSize = node.getParaDecList().size();
        ParaList = new ArrayList<>();
        for (ParameterNode para : node.getParaDecList()) {
            VariableEntity mx_var = new VariableEntity(father, para);
            if (scope.hasVariable(mx_var.getIdentifier())) {
                SemanticChecker.logger.severe("Two function parameter cannot have the same name.",
                        node.GetLocation());
            }
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
