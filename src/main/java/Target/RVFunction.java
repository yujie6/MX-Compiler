package Target;

import IR.Function;
import Optim.FuncAnalysis.LoopAnalysis;

import java.util.ArrayList;

public class RVFunction {

    Function irFunction;
    String identifier;
    ArrayList<RVBlock> rvBlockList;
    boolean isExternal;
    public LoopAnalysis LA;
    public int deltaStack;


    public RVFunction(Function function) {
        this.irFunction = function;
        this.rvBlockList = new ArrayList<>();
        this.deltaStack = 0;
        this.LA = function.getLA();
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

    public RVBlock getHeadBlock() {
        return rvBlockList.get(0);
    }

    public RVBlock getRetBlock() {
        return rvBlockList.get(rvBlockList.size() - 1);
    }

    public int getDeltaStack() {
        return deltaStack;
    }

    public int getArgNum() {
        return irFunction.getParameterList().size();
    }

    public void addRVBlock(RVBlock block) {
        this.rvBlockList.add(block);
    }

}
