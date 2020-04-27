package Target;

import IR.Function;

import java.util.ArrayList;

public class RVFunction {

    Function irFunction;
    ArrayList<RVBlock> rvBlockList;


    public RVFunction(Function function) {
        this.irFunction = function;
        this.rvBlockList = new ArrayList<>();
    }

    public void addRVBlock(RVBlock block) {
        this.rvBlockList.add(block);
    }

}
