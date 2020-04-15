package Optim.FuncAnalysis;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.Instruction;
import Optim.MxOptimizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Create dominator tree by Lengauer Tarjan Algorithm
 *
 * And build post dom tree for dependence graph
 *
 */
public class DomTreeBuilder {

    Function function;
    public HashMap<BasicBlock, DomNode> domTree;
    public HashMap<BasicBlock, LinkedHashSet<BasicBlock>> domFrontier;
    BasicBlock[] parent;
    BasicBlock[] a;
    BasicBlock[] idom;
    BasicBlock[] sdom;
    BasicBlock[] rdom;
    BasicBlock[] best;
    BasicBlock[] vertex;
    LinkedHashSet<Integer>[] bucket;
    private int dfn, sum;

    public DomTreeBuilder(Function function) {
        domTree = new HashMap<>();
        this.function = function;
        this.domFrontier = new HashMap<>();
    }

    public void init() {
        sum = function.getBlockList().size();
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

    public void build() {
        init();
        dfs(null, function.getHeadBlock());
        computeIdom();
        computeDF();
    }

    public boolean dominates(BasicBlock defBB, BasicBlock useBB) {
        if (defBB == useBB) return true;
        return domTree.get(defBB).dominates(domTree.get(useBB));
    }


    private void Link(int parent, int i) {
        a[i] = vertex[parent];
    }

    private int Eval(BasicBlock v) {
        BasicBlock A = a[v.dfnOrder];
        if (A == null) return v.dfnOrder;
        Compress(v);
        return best[v.dfnOrder].dfnOrder;
    }

    private void Compress(BasicBlock v) {
        BasicBlock A = a[v.dfnOrder];
        if (a[A.dfnOrder] == null) return;
        Compress(A);
        if (sdom[best[v.dfnOrder].dfnOrder].dfnOrder > sdom[best[A.dfnOrder].dfnOrder].dfnOrder) {
            best[v.dfnOrder] = best[A.dfnOrder];
        }
        a[v.dfnOrder] = a[A.dfnOrder];
    }

    private void computeIdom() {
        // Compute sdom and rdom by reverse order
        for (int i = sum - 1; i > 0; i--) {
            BasicBlock p = parent[i];
            for (BasicBlock v : vertex[i].predecessors) {
                int u = Eval(v);
                if (sdom[i].dfnOrder > sdom[u].dfnOrder) {
                    sdom[i] = sdom[u];
                }
            }
            bucket[sdom[i].dfnOrder].add(i);
            Link(p.dfnOrder, i);
            for (int v : bucket[p.dfnOrder]) {
                int u = Eval(vertex[v]);
                idom[v] = sdom[u].dfnOrder < sdom[v].dfnOrder ? vertex[u] : p;
            }
            bucket[p.dfnOrder].clear();
        }

        for (int i = 1; i < sum; i++) {
            if (idom[i] != sdom[i]) {
                idom[i] = idom[idom[i].dfnOrder];
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
        domTree.put(vertex[0], root);
        for (int i = 1; i < sum; i++) {
            DomNode t = new DomNode(vertex[i]);
            domTree.put(vertex[i], t);
            t.idom = domTree.get(idom[i]);
            t.idom.addChild(t);
        }

        MxOptimizer.logger.fine("Dom tree build on function '" + function.getIdentifier() + "' done");

    }

    private void computeDF() {
        computeDF(domTree.get(function.getHeadBlock()));
    }

    private void computeDF(DomNode node) {
        LinkedHashSet<BasicBlock> df = new LinkedHashSet<>();
        // this loop computer df_local
        for (BasicBlock y : node.block.successors) {
            if (domTree.get(y).idom != node) {
                df.add(y);
            }
        }
        // this loop compute df_up
        for (DomNode child : node.children) {
            computeDF(child);
            for (BasicBlock w : domFrontier.get(child.block)) {
                if (!node.dominates(domTree.get(w)) || w == node.block) {
                    df.add(w);
                }
            }
        }

        domFrontier.put(node.block, df);
    }

    private void dfs(BasicBlock father, BasicBlock child) {
        if (child.dfnOrder == -1) {
            vertex[dfn] = child;
            parent[dfn] = father;
            sdom[dfn] = child;
            best[dfn] = child;
            a[dfn] = null;
            bucket[dfn].clear();
            child.dfnOrder = dfn;
            dfn += 1;
            for (BasicBlock bb : child.successors) {
                dfs(child, bb);
            }
        }
    }


}
