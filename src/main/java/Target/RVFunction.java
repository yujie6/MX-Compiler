package Target;

import IR.Function;

import java.util.ArrayList;

public class RVFunction {

    Function irFunction;
    ArrayList<RVBlock> rvBlockList;
    int deltaStack;


    public RVFunction(Function function) {
        this.irFunction = function;
        this.rvBlockList = new ArrayList<>();
        this.deltaStack = 0;
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
