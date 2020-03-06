package MxEntity;

import AST.*;
import Frontend.Scope;
import Frontend.SemanticChecker;

import java.lang.reflect.Member;
import java.util.HashMap;

public class ClassEntity extends Entity {

    private HashMap<String ,Integer> offsetMap;
    private int memberNum;
    public ClassEntity(String id, Scope father) {
        super(id);
        setScope(father);
        offsetMap = new HashMap<>();
    }

    public ClassEntity(Scope father, ClassDecNode node) {
        super(node.getIdentifier());
        scope = new Scope(father);
        memberNum = 0;
        offsetMap = new HashMap<>();
        scope.inClass = true;
        for (VariableDecNode var_list : node.getVarNodeList()) {
            Type DecType = var_list.getType();
            for (VarDecoratorNode var : var_list.getVarDecoratorList()) {
                VariableEntity mx_var = new VariableEntity(scope, var, DecType);
                mx_var.setIdentifier(node.getIdentifier() + '.' + var.getIdentifier());
                mx_var.setScopeLevel(2);
                if (scope.hasVariable(node.getIdentifier() + '.' + var.getIdentifier())) {
                    SemanticChecker.logger.severe("The member " + var.getIdentifier() + " has " +
                            "been defined twice in the class '" + node.getIdentifier() + "'.", node.GetLocation());
                }
                scope.defineVariable(mx_var);
                offsetMap.put(mx_var.getIdentifier(), memberNum);
                memberNum += 1;
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

    public int getMemberOffset(String name) {
        return offsetMap.get(getIdentifier() + '.' + name);
    }

    public boolean hasMember(String name) {
        return scope.hasVariable(getIdentifier() + '.' + name);
    }

    public boolean hasMethod(String name) {
        return scope.hasFunction(getIdentifier() + '.' + name);
    }

    public VariableEntity getMember(String name) {
        return scope.GetVariable(getIdentifier() + '.' + name);
    }


}
