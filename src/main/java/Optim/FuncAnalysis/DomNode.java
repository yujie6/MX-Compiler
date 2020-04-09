package Optim.FuncAnalysis;

import IR.BasicBlock;

import java.util.ArrayList;

public class DomNode {
    public DomNode idom;
    public ArrayList<DomNode> children;
    public BasicBlock block;
    public DomNode(BasicBlock block1) {
        this.block = block1;
        this.children = new ArrayList<>();
    }

    public boolean dominates(DomNode other) {
        return children.contains(other);
    }

    public void addChild(DomNode child) {
        children.add(child);
    }
}