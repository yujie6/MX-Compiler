package Target;

import IR.GlobalVariable;

import java.util.ArrayList;

public class RVModule {
    public ArrayList<RVFunction> rvFunctions;
    public ArrayList<RVGlobal> rvGlobals;

    public RVModule() {
        this.rvFunctions = new ArrayList<>();
        this.rvGlobals = new ArrayList<>();
    }

    public void addFunction(RVFunction rvFunction) {
        this.rvFunctions.add(rvFunction);
    }

    public void addGlobalVar(RVGlobal rvGlobal) {
        this.rvGlobals.add(rvGlobal);
    }



}
