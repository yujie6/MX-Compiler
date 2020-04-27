package Optim.FuncAnalysis;

import IR.BasicBlock;
import IR.Function;
import Optim.MxOptimizer;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * This file implements control dependence graph builder
 */
public class CDGBuilder {
    public Function function;


    public HashMap<BasicBlock, LinkedHashSet<BasicBlock>> postDomFrontier;
    public HashMap<BasicBlock, DomNode> postDomTree;
    private BasicBlock entryBlock;
    BasicBlock[] parent;
    BasicBlock[] a;
    BasicBlock[] idom;
    BasicBlock[] sdom;
    BasicBlock[] rdom;
    BasicBlock[] best;
    BasicBlock[] vertex;
    LinkedHashSet<Integer>[] bucket;
    private int dfn, sum;


    public CDGBuilder(Function function1) {
        this.function = function1;
        postDomTree = new HashMap<>();
        postDomFrontier = new HashMap<>();
    }

    public void build() {
        entryBlock = new BasicBlock(null, "entryNode");
        entryBlock.successors.add(function.getHeadBlock());
        entryBlock.successors.add(function.getRetBlock());
        function.getHeadBlock().predecessors.add(entryBlock);
        function.getRetBlock().predecessors.add(entryBlock);
        init();
        dfs(null, function.getRetBlock());
        computePostIdom();
        computePostDF();

        function.getHeadBlock().predecessors.remove(entryBlock);
        function.getRetBlock().predecessors.remove(entryBlock);
    }

    private void computePostDF() {
        computePostDF(postDomTree.get(function.getRetBlock()));
    }

    private void computePostDF(DomNode node) {
        LinkedHashSet<BasicBlock> df = new LinkedHashSet<>();
        // this loop computer df_local
        for (BasicBlock y : node.block.successors) {
            if (postDomTree.get(y).idom != node) {
                df.add(y);
            }
        }
        // this loop compute df_up
        for (DomNode child : node.children) {
            computePostDF(child);
            for (BasicBlock w : postDomFrontier.get(child.block)) {
                if (!node.dominates(postDomTree.get(w)) || w == node.block) {
                    df.add(w);
                }
            }
        }

        postDomFrontier.put(node.block, df);
    }

    private void computePostIdom() {
        for (int i = sum - 1; i > 0; i--) {
            BasicBlock p = parent[i];
            if (vertex[i] == null || vertex[i].successors == null) {
                System.out.println("it's fine");
            }
            for (BasicBlock v : vertex[i].successors) {
                int u = Eval(v);
                if (sdom[i].postDfnOrder > sdom[u].postDfnOrder) {
                    sdom[i] = sdom[u];
                }
            }
            bucket[sdom[i].postDfnOrder].add(i);
            Link(p.postDfnOrder, i);
            for (int v : bucket[p.postDfnOrder]) {
                int u = Eval(vertex[v]);
                idom[v] = sdom[u].postDfnOrder < sdom[v].postDfnOrder ? vertex[u] : p;
            }
            bucket[p.postDfnOrder].clear();
        }

        for (int i = 1; i < sum; i++) {
            if (idom[i] != sdom[i]) {
                idom[i] = idom[idom[i].postDfnOrder];
            }
        }
        idom[0] = null;

        for (int i = 1; i < sum; i++) {
            if (idom[i] == null) {
                MxOptimizer.logger.severe("Idom build fail!");
                System.exit(1);
            }
        }

        DomNode root = new DomNode(vertex[0]);
        postDomTree.put(vertex[0], root);
        for (int i = 1; i < sum; i++) {
            DomNode t = new DomNode(vertex[i]);
            postDomTree.put(vertex[i], t);
            t.idom = postDomTree.get(idom[i]);
            t.idom.addChild(t);
        }

        MxOptimizer.logger.fine("post Dom tree build on function '" + function.getIdentifier() + "' done");
    }


    private void init() {
        sum = function.getBlockList().size() + 1;
        parent = new BasicBlock[sum];
        idom = new BasicBlock[sum];
        sdom = new BasicBlock[sum];
        vertex = new BasicBlock[sum];
        rdom = new BasicBlock[sum];
        best = new BasicBlock[sum];
        a = new BasicBlock[sum];
        bucket = new LinkedHashSet[sum];
        for (int i = 0; i < sum; i++)
            bucket[i] = new LinkedHashSet<>();
        dfn = 0;
    }

    private void Link(int parent, int i) {
        a[i] = vertex[parent];
    }

    private int Eval(BasicBlock v) {
        BasicBlock A = a[v.postDfnOrder];
        if (A == null) return v.postDfnOrder;
        Compress(v);
        return best[v.postDfnOrder].postDfnOrder;
    }

    private void Compress(BasicBlock v) {
        BasicBlock A = a[v.postDfnOrder];
        if (a[A.postDfnOrder] == null) return;
        Compress(A);
        if (sdom[best[v.postDfnOrder].postDfnOrder].postDfnOrder > sdom[best[A.postDfnOrder].postDfnOrder].postDfnOrder) {
            best[v.postDfnOrder] = best[A.postDfnOrder];
        }
        a[v.postDfnOrder] = a[A.postDfnOrder];
    }

    private void dfs(BasicBlock father, BasicBlock child) {
        if (child.postDfnOrder == -1) {
            vertex[dfn] = child;
            parent[dfn] = father;
            sdom[dfn] = child;
            best[dfn] = child;
            a[dfn] = null;
            bucket[dfn].clear();
            child.postDfnOrder = dfn;
            dfn += 1;
            for (BasicBlock bb : child.predecessors) {
                dfs(child, bb);
            }
        }
    }

}
