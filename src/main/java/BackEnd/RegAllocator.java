package BackEnd;

import Optim.Transformation.CommonSubexElim;
import Target.*;
import Target.RVInstructions.RVInstruction;
import Target.RVInstructions.RVMove;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Build, simplify, spill and select
 */
public class RegAllocator extends RVPass implements AsmVisitor<Object> {
    /*
    virtual registers, not preColored or processed,
    used to initialize the below workList
     */
    private LinkedList<VirtualReg> initial;
    private HashSet<VirtualReg> preColored;

    private LinkedList<VirtualReg> simplifyWorkList;
    private LinkedList<VirtualReg> freezeWorkList;
    private LinkedList<VirtualReg> spillWorkList;

    private LinkedList<VirtualReg> spilledNodes;
    private HashSet<VirtualReg> coloredNodes;
    private HashSet<VirtualReg> coalescedNodes;

    /*
    Each move inst is in exactly one of these 5 move sets.
     */
    private LinkedList<RVMove> workListMoves;
    private HashSet<RVMove> coalescedMoves;
    private HashSet<RVMove> constrainedMoves;
    private HashSet<RVMove> frozenMoves;
    private HashSet<RVMove> activeMoves;

    /*

     */
    private Stack<VirtualReg> selectStack;
    private final int K = 20; // allocatable registers
    private HashMap<VirtualReg, LinkedList<RVMove>> moveListMap;
    private RVFunction curFunction;


    private LinkedList<RVMove> moveList(VirtualReg node) {
        if (moveListMap.containsKey(node)) {
            return moveListMap.get(node);
        } else {
            LinkedList<RVMove> list = new LinkedList<>();
            moveListMap.put(node, list);
            return list;
        }
    }

    /*
    use 2 ways to maintain the connection of nodes,
    to be fast for 2 kinds of queries
     */
    private HashSet<edge> adjacentSet;

    private class edge {
        public VirtualReg u, v;

        public edge(VirtualReg u, VirtualReg v) {
            this.u = u;
            this.v = v;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof edge) {
                return u == ((edge) obj).u && v == ((edge) obj).v;
            }
            return false;
        }
    }


    public RegAllocator(RVModule topModule) {
        super(topModule);
        this.adjacentSet = new HashSet<>();
        this.simplifyWorkList = new LinkedList<>();
        this.freezeWorkList = new LinkedList<>();
        this.spillWorkList = new LinkedList<>();

        this.spilledNodes = new LinkedList<>();
        this.coalescedNodes = new HashSet<>();
        this.coloredNodes = new LinkedHashSet<>();

        this.moveListMap = new HashMap<>();
        this.workListMoves = new LinkedList<>();
        this.initial = new LinkedList<>();
        this.selectStack = new Stack<>();
        this.preColored = new HashSet<>(InstSelector.fakePhyRegMap.values());
    }

    @Override
    public void run() {
        visit(TopModule);
    }

    private boolean processDone() {
        return simplifyWorkList.isEmpty() && workListMoves.isEmpty()
                && freezeWorkList.isEmpty() && spillWorkList.isEmpty();
    }

    @Override
    public Object visit(RVFunction rvFunction) {
        curFunction = rvFunction;
        for (RVBlock BB : rvFunction.getRvBlockList()) {
            for (RVInstruction inst : BB.rvInstList) {
                initial.addAll(inst.getUseRegs());
                initial.addAll(inst.getDefRegs());
            }
        }
        initial.removeAll(preColored);

        clear();
        Build();
        MakeWorkList();
        do {
            if (!simplifyWorkList.isEmpty()) {
                Simplify();
            } else if (workListMoves.isEmpty()) {
                Coalesce();
                ;
            } else if (freezeWorkList.isEmpty()) {
                Freeze();
            } else if (spillWorkList.isEmpty()) {
                SelectSpill();
            }
        } while (!processDone());
        AssignColors();
        if (!spilledNodes.isEmpty()) {
            RewriteProgram();
            visit(rvFunction);
        }
        return null;
    }

    private void clear() {
        this.adjacentSet.clear();
        this.workListMoves.clear();
        this.spillWorkList.clear();
        this.simplifyWorkList.clear();
        this.freezeWorkList.clear();
        this.activeMoves.clear();
        this.coalescedMoves.clear();
        this.constrainedMoves.clear();
        this.coloredNodes.clear();
        this.selectStack.clear();
    }


    @Override
    public Object visit(RVModule rvModule) {
        for (RVFunction rvFunction : rvModule.rvFunctions) {
            visit(rvFunction);
        }
        return null;
    }

    private void Build() {

        for (RVBlock BB : curFunction.getRvBlockList()) {
            HashSet<VirtualReg> live = new HashSet<>(BB.liveOutSet);
            for (int i = BB.rvInstList.size() - 1; i >= 0; i--) {
                RVInstruction inst = BB.rvInstList.get(i);
                if (inst instanceof RVMove) {
                    live.removeAll(inst.getUseRegs());
                    HashSet<VirtualReg> tmp = new HashSet<>(inst.getDefRegs());
                    tmp.addAll(inst.getUseRegs());
                    for (VirtualReg n : tmp) {
                        moveList(n).add((RVMove) inst);
                    }
                    this.workListMoves.add((RVMove) inst);
                }
                live.addAll(inst.getDefRegs());
                for (VirtualReg d : inst.getDefRegs()) {
                    for (VirtualReg l : live) {
                        addEdge(d, l);
                    }
                }
                live.removeAll(inst.getDefRegs());
                live.addAll(inst.getUseRegs());
            }
        }
    }

    private void addEdge(VirtualReg u, VirtualReg v) {
        edge uv = new edge(u, v);
        if (!adjacentSet.contains(uv) && u != v) {
            adjacentSet.add(uv);
            adjacentSet.add(new edge(v, u));
            if (!u.isPreColored()) {
                u.addNeighbor(v);
            }
            if (!v.isPreColored()) {
                v.addNeighbor(u);
            }
        }
    }

    private boolean moveRelated(VirtualReg n) {
        return !getNodeMoves(n).isEmpty();
    }

    private HashSet<VirtualReg> getAdjacent(VirtualReg n) {
        HashSet<VirtualReg> adj = new HashSet<>(n.neighbors);
        adj.removeAll(selectStack);
        adj.removeAll(coalescedNodes);
        return adj;
    }

    private HashSet<RVMove> getNodeMoves(VirtualReg n) {
        HashSet<RVMove> nodeMoves = new HashSet<>(moveList(n));
        nodeMoves.removeIf(i -> {
            return activeMoves.contains(i) || workListMoves.contains(i);
        });
        return nodeMoves;
    }

    private void MakeWorkList() {
        while (!initial.isEmpty()) {
            VirtualReg n = initial.pop();
            if (n.degree >= K) {
                spillWorkList.add(n);
            } else if (moveRelated(n)) {
                freezeWorkList.add(n);
            } else {
                simplifyWorkList.add(n);
            }
        }
    }

    private void DecrementDegree(VirtualReg m) {
        m.degree = m.degree - 1;
        if (m.degree == K - 1) {
            HashSet<VirtualReg> t = getAdjacent(m);
            t.add(m);
            EnableMoves(t);
            spillWorkList.remove(m);
            if (moveRelated(m)) {
                freezeWorkList.add(m);
            } else
                simplifyWorkList.add(m);
        }
    }

    private void EnableMoves(Set<VirtualReg> nodes) {
        for (VirtualReg n : nodes) {
            for (RVMove m : getNodeMoves(n)) {
                activeMoves.remove(m);
                workListMoves.add(m);
            }
        }
    }

    private void Simplify() {
        VirtualReg n = simplifyWorkList.pop();
        this.selectStack.push(n);
        for (VirtualReg m : getAdjacent(n)) {
            DecrementDegree(m);
        }

    }

    private void AddWorkList(VirtualReg u) {
        if (!preColored.contains(u) && !(moveRelated(u)) && u.degree < K) {
            freezeWorkList.remove(u);
            simplifyWorkList.add(u);
        }
    }

    private boolean OK(VirtualReg t, VirtualReg r) {
        return t.degree < K || preColored.contains(t) ||
                adjacentSet.contains(new edge(t, r));
    }

    private boolean conservative(Set<VirtualReg> nodes) {
        int k = 0;
        for (VirtualReg n : nodes) {
            if (n.degree >= K) {
                k++;
            }
        }
        return k < K;
    }

    private VirtualReg getAlias(VirtualReg n) {
        if (coalescedNodes.contains(n)) {
            return getAlias(n.alias);
        }
        return n;
    }

    private boolean checkCombine(VirtualReg u, VirtualReg v) {
        if (preColored.contains(u)) {
            return checkCombine1(u, v);
        } else {
            return checkCombine2(u, v);
        }
    }

    private boolean checkCombine1(VirtualReg u, VirtualReg v) {
        for (VirtualReg t : getAdjacent(v)) {
            if (!OK(t, u)) return false;
        } return true;
    }

    private boolean checkCombine2(VirtualReg u, VirtualReg v) {
        HashSet<VirtualReg> nodes = getAdjacent(u);
        nodes.addAll(getAdjacent(v));
        return conservative(nodes);
    }

    /**
     * @param u, v: combine v to u, core function for coalesce
     */
    private void Combine(VirtualReg u, VirtualReg v) {
        if (freezeWorkList.contains(v)) {
            freezeWorkList.remove(v);
        } else spillWorkList.remove(v);
        coalescedNodes.add(v);
        v.alias = u;
        moveList(u).addAll(moveList(v));
        EnableMoves(Set.of(v));
        for (VirtualReg t : getAdjacent(v)) {
            addEdge(t, u);
            DecrementDegree(t);
        }
        if (u.degree >= K && freezeWorkList.contains(u)) {
            freezeWorkList.remove(u);
            spillWorkList.add(u);
        }
    }

    private void Coalesce() {
        RVMove m = workListMoves.pop();
        VirtualReg x = getAlias(m.getDest());
        VirtualReg y = getAlias(m.getSrc());
        VirtualReg u, v;
        if (preColored.contains(y)) {
            u = y; v = x;
        } else {
            u = x; v = y;
        }
        if (u == v) {
            coalescedMoves.add(m);
            AddWorkList(u);
        } else if (preColored.contains(v) || adjacentSet.contains(new edge(u, v))) {
            constrainedMoves.add(m);
            AddWorkList(u);
            AddWorkList(v);
        } else if ( checkCombine(u,v) ) {
            coalescedMoves.add(m);
            Combine(u, v);
            AddWorkList(u);
        } else {
            activeMoves.add(m);
        }

    }

    private void freezeMoves(VirtualReg u) {
        for (RVMove m : getNodeMoves(u)) {
            VirtualReg x = m.getDest(), y = m.getSrc(), v;
            if (getAlias(y) == u) {
                v = getAlias(x);
            } else {
                v = getAlias(y);
            }
            activeMoves.remove(m);
            frozenMoves.add(m);
            if (freezeWorkList.contains(v) && getNodeMoves(v).isEmpty()) {
                freezeWorkList.remove(v);
                simplifyWorkList.add(v);
            }
        }
    }

    private void Freeze() {
        VirtualReg u = freezeWorkList.pop();
        simplifyWorkList.add(u);
        freezeMoves(u);
    }

    private VirtualReg selectFavorite() {
        return null;
    }

    private void SelectSpill() {
        VirtualReg m = selectFavorite();
        spillWorkList.remove(m);
        simplifyWorkList.add(m);
        freezeMoves(m);
    }

    private void AssignColors() {
        while (!selectStack.isEmpty()) {
            VirtualReg n = selectStack.pop();
            Set<Integer> okColors = new HashSet<>();
            for (int i = 0; i < K; i++) okColors.add(i);
            for (VirtualReg w : n.neighbors) {
                VirtualReg w_alias = getAlias(w);
                if (preColored.contains(w_alias) ||
                coloredNodes.contains(w_alias)) {
                    okColors.remove(w_alias.color);
                }
            }
            if (okColors.isEmpty()) {
                spilledNodes.add(n);
            }
            else {
                coloredNodes.add(n);
                int c = okColors.iterator().next();
                n.color = c;
            }
        }

        for (VirtualReg n : coalescedNodes) {
            n.color = getAlias(n).color;
        }
    }

    private void RewriteProgram() {
        // allocate memory for each v in spilledNodes
        // create a new temporary vi for each use and def
        Set<VirtualReg> newTemps = new HashSet<>();
        for (VirtualReg v : spilledNodes) {
            VirtualReg vi = new VirtualReg("tmp for spilled node");
        }

        spilledNodes.clear();
        initial.clear();
        initial.addAll(coloredNodes);
        initial.addAll(coalescedNodes);
        initial.addAll(newTemps);
        coloredNodes.clear();
        coalescedNodes.clear();
    }


    @Override
    public Object visit(RVBlock rvBlock) {

        return null;
    }
}
