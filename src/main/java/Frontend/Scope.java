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

    public Scope(Scope other) {
        this.VarMap = new HashMap<>(other.VarMap);
        this.ClassMap = new HashMap<>(other.ClassMap);
        this.FuncMap = new HashMap<>(other.FuncMap);
        this.inClass = other.inClass;
        this.inFunction = other.inFunction;
        this.LoopLevel = other.LoopLevel;
        this.FuncRetType = new Type(other.FuncRetType);
    }

    public void clear() {
        VarMap.clear();
        FuncMap.clear();
        ClassMap.clear();
    }

    public void setFuncRetType(Type funcRetType) {
        FuncRetType = funcRetType;
    }

    public Type getFuncRetType() {
        return FuncRetType;
    }

    public void defineVariable(VariableEntity mx_variable) {
        VarMap.put(mx_variable.getIdentifier(), mx_variable);
    }

    public void defineFunction(FunctionEntity mx_function) {
        // TODO: diff func and method
        if (mx_function.isMethod()) {
            String funcName = mx_function.getClassName() + '.' + mx_function.getIdentifier();
            if (FuncMap.containsKey(funcName)) {
                throw new MXError("The function " + mx_function.getIdentifier() +
                        " has been defined twice");
            }
            FuncMap.put(funcName, mx_function);
        } else {
            if (FuncMap.containsKey(mx_function.getIdentifier())) {
                throw new MXError("The function " + mx_function.getIdentifier() +
                        " has been defined twice");
            }
            FuncMap.put(mx_function.getIdentifier(), mx_function);
        }
    }

    public void defineClass(ClassEntity mx_class) {
        if (ClassMap.containsKey(mx_class.getIdentifier())) {
            throw new MXError("The class " + mx_class.getIdentifier() +
                    " has been defined twice");
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
                    " has been used before defined");
        }
        return VarMap.get(name);
    }

    public FunctionEntity GetFunction(String name) {
        if (!FuncMap.containsKey(name)) {
            //SemanticChecker.logger.severe();
            throw new MXError("The function " + name +
                    " is not defined.");
        }
        return FuncMap.get(name);
    }

    public ClassEntity GetClass(String name) {
        if (!ClassMap.containsKey(name)) {
            throw new MXError("The class " + name +
                    " is not defined.");
        }
        return ClassMap.get(name);
    }

}
