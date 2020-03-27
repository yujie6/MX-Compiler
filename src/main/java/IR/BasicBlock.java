package IR;

import IR.Instructions.BranchInst;
import IR.Instructions.Instruction;

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
    private BasicBlock prev, next;
    private Instruction HeadInst, TailInst;

    private static int BlockNum = 1;
    private String Label;

    private Set<BasicBlock> predecessors;
    private Set<BasicBlock> successors;

    public BasicBlock(Function parent, String id) {
        super(ValueType.BASIC_BLOCK);
        this.Parent = parent;
        this.Identifier = id;
        HeadInst = null;
        TailInst = null;
        prev = null;
        next = null;
        predecessors = new LinkedHashSet<>();
        successors = new LinkedHashSet<>();
        InstList = new LinkedList<>();
        Label = String.valueOf(BlockNum);
        BlockNum += 1;
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
        } else {
            TailInst.setNext(inst);
            inst.setPrev(TailInst);
        }
        TailInst = inst;
        InstList.addLast(inst);
    }

    public void AddInstAtTop(Instruction inst) {
        if (isEmpty()) {
            TailInst = inst;
        } else {
            HeadInst.setPrev(inst);
            inst.setNext(HeadInst);
        }
        HeadInst = inst;
        InstList.addFirst(inst);
    }

    public boolean endWithBranch() {
        return TailInst instanceof BranchInst;
    }

    @Override
    public void accept(IRVisitor<IRBaseNode> visitor) {
        visitor.visit(this);
    }

    public BasicBlock getPrev() {
        return prev;
    }

    public void setPrev(BasicBlock prev) {
        this.prev = prev;
    }

    public BasicBlock getNext() {
        return next;
    }

    public void setNext(BasicBlock next) {
        this.next = next;
    }

    public String getLabel() {
        return Label;
    }

    public void setLabel(String label) {
        Label = label;
    }
}
