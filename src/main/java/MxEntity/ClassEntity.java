package MxEntity;

import AST.*;
import Frontend.Scope;

public class ClassEntity extends Entity {

    public ClassEntity(String id, Scope father) {
        super(id);
        setScope(father);
    }

    public ClassEntity(Scope father, ClassDecNode node) {
        super(node.getIdentifier());
        scope = new Scope(father);
        scope.inClass = true;
        for (VariableDecNode var_list : node.getVarNodeList()) {
            Type DecType = var_list.getType();
            for (VarDecoratorNode var : var_list.getVarDecoratorList()) {
                VariableEntity mx_var = new VariableEntity(scope, var, DecType);
                // mx_var.setIdentifier(node.getIdentifier() + '.' + var.getIdentifier());
                mx_var.setIdentifier(var.getIdentifier());
                mx_var.setScopeLevel(2);
                scope.defineVariable(mx_var);
            }
        }

        for (MethodDecNode method : node.getMethodNodeList()) {
            // scope here is incomplete
            FunctionEntity mx_method = new FunctionEntity(scope, method,
                    true, getIdentifier());
            scope.defineFunction(mx_method);
        }

//        for (MethodDecNode method : node.getMethodNodeList()) {
//            scope.GetFunction(this.getIdentifier() + '.' + method.getIdentifier())
//                    .setScope(scope);
//        }
    }

    public FunctionEntity getMethod(String name) {
        // must specify class name
        return scope.GetFunction(getIdentifier() + '.' + name);
    }

    public VariableEntity getMember(String name) {
        return scope.GetVariable(name);
    }


}
