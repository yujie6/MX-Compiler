package MxEntity;

import AST.ClassDecNode;
import AST.MethodDecNode;
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
        for (MethodDecNode method : node.getMethodNodeList()) {
            // scope here is incomplete
            FunctionEntity mx_method = new FunctionEntity(scope, method,
                    true, getIdentifier());
            scope.defineFunction(mx_method);
        }

        for (MethodDecNode method : node.getMethodNodeList()) {
            scope.GetFunction(this.getIdentifier() + '.' + method.getIdentifier())
                    .setScope(scope);
        }
    }

    public FunctionEntity getMethod(String name) {
        // must specify class name
        return scope.GetFunction(getIdentifier() + '.' + name);
    }

    public VariableEntity getMember(String name) {
        return scope.GetVariable(getIdentifier() + '.' + name);
    }


}
