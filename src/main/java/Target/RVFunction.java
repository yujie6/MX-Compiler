package Target;

import IR.Function;

import java.util.ArrayList;

public class RVFunction {

    Function irFunction;
    String identifier;
    ArrayList<RVBlock> rvBlockList;
    boolean isExternal;
    int deltaStack;


    public RVFunction(Function function) {
        this.irFunction = function;
        this.rvBlockList = new ArrayList<>();
        this.deltaStack = 0;
        this.identifier = function.getIdentifier();
        this.isExternal = function.isExternal();
    }

    public ArrayList<RVBlock> getRvBlockList() {
        return rvBlockList;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int allocaOnStack() {
        this.deltaStack -= 4;
        return this.deltaStack;
    }

    public int getDeltaStack() {
        return deltaStack;
    }

    public void addRVBlock(RVBlock block) {
        this.rvBlockList.add(block);
    }

}
