package Frontend;

import AST.FunctionDecNode;
import AST.Type;
import AST.VariableDecNode;
import MxEntity.*;
import Tools.MXError;
import java.util.HashMap;

public class Scope {
    static int ScopeId;
    private HashMap<String, VariableEntity> VarMap;
    private HashMap<String, FunctionEntity> FuncMap;
    private HashMap<String, ClassEntity> ClassMap;
    int LoopLevel;
    public boolean inFunction, inClass;
    private Type FuncRetType;
    public Scope() {
        LoopLevel = 0;
        VarMap = new HashMap<>();
        FuncMap = new HashMap<>();
        ClassMap = new HashMap<>();
        inFunction = false;
        inClass = false;
    }

    public void setFuncRetType(Type funcRetType) {
        FuncRetType = funcRetType;
    }

    public Type getFuncRetType() {
        return FuncRetType;
    }

    public void defineVariable(VariableEntity mx_variable) {
        if (VarMap.containsKey(mx_variable.getIdentifier())) {
            if (VarMap.get(mx_variable.getIdentifier()).getScope() == this) {
                // mx_variable's scope is just a reference of local scope
                throw new MXError("The variable " + mx_variable.getIdentifier() +
                        " has been defined twice", mx_variable.getLocation());
            }
        }
        VarMap.put(mx_variable.getIdentifier(), mx_variable);
    }

    public void defineFunction(FunctionEntity mx_function) {
        // TODO: diff func and method
        if (mx_function.isMethod()) {
            FuncMap.put(mx_function.getClassName() + '.' + mx_function.getIdentifier(), mx_function);
        } else FuncMap.put(mx_function.getIdentifier(), mx_function);
    }

    public void defineClass(ClassEntity mx_class) {
        if (ClassMap.containsKey(mx_class.getIdentifier())) {
            throw new MXError("The class " + mx_class.getIdentifier() +
                    "has been defined twice");
        }
        ClassMap.put(mx_class.getIdentifier(), mx_class);
    }

    public boolean hasClass(String name) {
        return ClassMap.containsKey(name);
    }

    public boolean hasFunction(String name) {
        return FuncMap.containsKey(name);
    }

    public boolean hasVariable(String name) {
        return VarMap.containsKey(name);
    }

    public VariableEntity GetVariable(String name) {
        if (!VarMap.containsKey(name)) {
            throw new MXError("The variable " + name +
                    "has been used before defined");
        }
        return VarMap.get(name);
    }

    public FunctionEntity GetFunction(String name) {
        if (!FuncMap.containsKey(name)) {
            throw new MXError("The function " + name +
                    " is not defined.");
        }
        return FuncMap.get(name);
    }

    public ClassEntity GetClass(String name) {
        if (!ClassMap.containsKey(name)) {
            throw new MXError("The class " + name +
                    "is not defined.");
        }
        return ClassMap.get(name);
    }

}
