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
        father.inClass = true;
        setScope(father);
        scope.inFunction = false;
        for (MethodDecNode method : node.getMethodNodeList()) {
            FunctionEntity mx_method = new FunctionEntity(scope, method,
                    true, getIdentifier());
            scope.defineFunction(mx_method);
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
