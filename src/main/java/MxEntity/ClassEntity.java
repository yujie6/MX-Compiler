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
        setScope(father);
        for (MethodDecNode method : node.getMethodNodeList()) {
            FunctionEntity mx_method = new FunctionEntity(scope, method);
            scope.defineFunction(mx_method);
        }
    }

}
