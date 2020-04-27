package Target;

import IR.GlobalVariable;

import java.util.ArrayList;

public class RVModule {
    public ArrayList<RVFunction> rvFunctionList;
    public ArrayList<GlobalVariable> globalVariableList;

    public RVModule() {
        this.rvFunctionList = new ArrayList<>();
        this.globalVariableList = new ArrayList<>();
    }

    public void addFunction(RVFunction rvFunction) {
        this.rvFunctionList.add(rvFunction);
    }



}
