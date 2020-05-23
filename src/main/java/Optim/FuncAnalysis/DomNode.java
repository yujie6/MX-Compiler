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
        DomNode t = other;
        while (t != null && t != this) {
            t = t.idom;
        }
        return t != null;
    }

    public void addChild(DomNode child) {
        children.add(child);
    }
}