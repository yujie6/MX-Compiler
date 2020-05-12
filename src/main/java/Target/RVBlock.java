package Target;

import IR.BasicBlock;
import Target.RVInstructions.RVInstruction;

import java.util.HashSet;
import java.util.LinkedList;

public class RVBlock {

    private static int rvBlockNum = 0;
    private int BlockID;
    private BasicBlock irBlock;
    private String label;
    public LinkedList<RVInstruction> rvInstList;
    public boolean isEntryBlock;
    public HashSet<RVBlock> predecessors;
    public HashSet<RVBlock> successors;
    public HashSet<VirtualReg> liveInSet;
    public HashSet<VirtualReg> liveOutSet;
    public HashSet<VirtualReg> gen;
    public HashSet<VirtualReg> kill;

    public RVBlock(BasicBlock block) {
        this.irBlock = block;
        this.rvInstList = new LinkedList<>();
        this.isEntryBlock = block.isEntryBlock();
        this.label = block.getIdentifier();
        this.liveInSet = new HashSet<>();
        this.liveOutSet = new HashSet<>();
        this.gen = new HashSet<>();
        this.kill = new HashSet<>();
        this.predecessors = new HashSet<>();
        this.successors = new HashSet<>();
        this.BlockID = rvBlockNum;
        rvBlockNum++;
    }

    @Override
    public String toString() {
        return "." + label + BlockID;
    }

    public String getLabel() {
        return "." + label + BlockID;
    }

    public void AddInst(RVInstruction inst) {
        rvInstList.add(inst);
    }

    public void AddInstAtTop(RVInstruction inst) {
        rvInstList.addFirst(inst);
    }


}
