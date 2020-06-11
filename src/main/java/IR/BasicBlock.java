package IR;

import IR.Instructions.BranchInst;
import IR.Instructions.CopyInst;
import IR.Instructions.Instruction;
import IR.Instructions.PhiInst;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * This class represents a single entry single exit section of the code, commonly known as a basic block by
 * the compiler community. The BasicBlock class maintains a list of Instructions, which form the body of the
 * block. Matching the language definition, the last element of this list of instructions is always a terminator
 * instruction. In addition to tracking the list of instructions that make up the block, the BasicBlock class
 * also keeps track of the Function that it is embedded into.
 * <p>
 * Note that BasicBlocks themselves are Values, because they are referenced by instructions like branches
 * and can go in the switch tables. BasicBlocks have type label.
 */

public class BasicBlock extends Value {
    private Function Parent;
    private String Identifier;
    private LinkedList<Instruction> InstList;

    public int dfnOrder, postDfnOrder;
    private Instruction HeadInst, TailInst;
    private ArrayList<CopyInst> copyInsts;

    private static int BlockNum = 1;
    private String Label;
    public int loopDepth = 0;

    public Set<BasicBlock> predecessors;
    public Set<BasicBlock> successors;

    public BasicBlock(Function parent, String id) {
        super(ValueType.BASIC_BLOCK);
        this.Parent = parent;
        this.Identifier = id;
        HeadInst = null;
        TailInst = null;
        predecessors = new LinkedHashSet<>();
        successors = new LinkedHashSet<>();
        InstList = new LinkedList<>();
        Label = String.valueOf(BlockNum);
        BlockNum += 1;
        this.dfnOrder = -1;
        this.postDfnOrder = -1;
        this.copyInsts = new ArrayList<>();
    }

    public BasicBlock(BasicBlock other) {
        super(ValueType.BASIC_BLOCK);
        this.Identifier = other.Identifier + "_copied";
        HeadInst = null;
        TailInst = null;
        predecessors = new LinkedHashSet<>();
        successors = new LinkedHashSet<>();
        InstList = new LinkedList<>();
        Label = String.valueOf(BlockNum);
        BlockNum += 1;
        this.dfnOrder = -1;
        this.postDfnOrder = -1;
        this.copyInsts = new ArrayList<>();
    }

    public void addSuccessor(BasicBlock basicBlock) {
        successors.add(basicBlock);
    }

    public void addPredecessor(BasicBlock basicBlock) {
        predecessors.add(basicBlock);
    }

    public Instruction getHeadInst() {
        return HeadInst;
    }

    public Instruction getTailInst() {
        return TailInst;
    }

    public String getIdentifier() {
        return Identifier;
    }

    public void setParent(Function parent) {
        Parent = parent;
    }

    public Function getParent() {
        return Parent;
    }

    public boolean isEmpty() {
        return HeadInst == null && TailInst == null;
    }

    public LinkedList<Instruction> getInstList() {
        return InstList;
    }

    public void AddInstAtTail(Instruction inst) {
        if (inst == null) return;
        if (isEmpty()) {
            HeadInst = inst;
        }
        inst.setParent(this);
        TailInst = inst;
        InstList.addLast(inst);
    }

    public void RemoveTailInst() {
        InstList.pollLast();
    }

    public boolean isEntryBlock() {
        return this == getParent().getHeadBlock();
    }

    public void AddInstAtTop(Instruction inst) {
        if (isEmpty()) {
            TailInst = inst;
        }
        inst.setParent(this);
        HeadInst = inst;
        InstList.addFirst(inst);
    }

    public void AddInstBeforeBranch(Instruction inst) {
        inst.setParent(this);
        InstList.add(InstList.size() - 1, inst); // where would you add ?
    }

    public void mergeWith(BasicBlock other) {
        Instruction br = getTailInst();
        if (br instanceof BranchInst) {
            br.eraseFromParent(); // >
        }
        other.successors.forEach(succ -> {
            succ.resetPhi(other, this);
            succ.predecessors.add(this);
            succ.predecessors.remove(other);
        });
        this.successors.addAll(other.successors);
        other.successors.clear();
        TailInst = other.TailInst;
        other.getInstList().forEach(inst -> {
            inst.setParent(this);
            InstList.add(inst);
        });
        if (other == Parent.getRetBlock()) {
            Parent.setRetBlock(this);
            Parent.getBlockList().remove(this);
            Parent.getBlockList().add(this);
        }
        Parent.getBlockList().remove(other);
    }

    public void resetPhi(BasicBlock from, BasicBlock to) {
        for (Instruction phi : InstList) {
            if (phi instanceof PhiInst) {
                int num = ((PhiInst) phi).getBranchNum();
                for (int i = 0; i < num; i++) {
                    if (((PhiInst) phi).getBlock(i) == from) {
                        ((PhiInst) phi).setBlock(i, to);
                    }
                    if (((PhiInst) phi).getBlock(i) == null) {
                        System.out.println("shit");
                    }
                }
            } else break;
        }
    }

    public BasicBlock split(Instruction inst) {
        BasicBlock newBB = new BasicBlock(getParent(), getIdentifier() + "_split");
        int index = InstList.indexOf(inst), size = InstList.size();
        for (int i = index + 1; i < size; i++) {
            newBB.AddInstAtTail(InstList.get(i));
            InstList.get(i).setParent(newBB);
        }
        for (int i = index + 1; i < size; i++) {
            InstList.remove(index + 1);
        }
        newBB.successors.addAll(successors);
        successors.forEach(succ -> {
            succ.resetPhi(this, newBB);
            succ.predecessors.remove(this);
            succ.predecessors.add(newBB);
        });
        successors.clear();
        newBB.HeadInst = newBB.getInstList().getFirst();
        newBB.TailInst = newBB.getInstList().getLast();
        // AddInstAtTail(new BranchInst(this, null, newBB, null));
        TailInst = InstList.getLast();
        Parent.AddBlockAfter(this, newBB);
        return newBB;
    }

    public boolean endWithBranch() {
        return TailInst instanceof BranchInst;
    }

    @Override
    public Object accept(IRVisitor<Object> visitor) {
        return visitor.visit(this);
    }

    public String getLabel() {
        return Label;
    }

    public ArrayList<CopyInst> getCopyInsts() {
        return copyInsts;
    }

    public void addCopyInst(CopyInst copyInst) {
        this.copyInsts.add(copyInst);
    }

    public void setLabel(String label) {
        Label = label;
    }
}
