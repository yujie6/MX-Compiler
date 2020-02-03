package Frontend;

import AST.FunctionDecNode;
import MxEntity.*;

import java.util.HashMap;

public class Scope {
    private HashMap<String, VariableEntity> VarMap;
    private HashMap<String, FunctionEntity> FuncMap;
    private HashMap<String, ClassEntity> ClassMap;

    public Scope() {
        VarMap = new HashMap<>();
        FuncMap = new HashMap<>();
        ClassMap = new HashMap<>();
    }

    public void defineVariable(VariableEntity mx_variable) {
        VarMap.put(mx_variable.getIdentifier(), mx_variable);
    }

    public void defineFunction(FunctionEntity mx_function) {
        FuncMap.put(mx_function.getIdentifier(), mx_function);
    }

    public void defineClass(ClassEntity mx_class) {
        ClassMap.put(mx_class.getIdentifier(), mx_class);
    }

    public VariableEntity GetVariable(String name) {
        return VarMap.get(name);
    }

    public FunctionEntity GetFunction(String name) {
        return FuncMap.get(name);
    }

    public ClassEntity GetClass(String name) {
        return ClassMap.get(name);
    }

}
