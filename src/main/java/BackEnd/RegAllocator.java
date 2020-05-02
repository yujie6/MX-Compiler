package BackEnd;

import Target.*;
import Target.RVInstructions.RVInstruction;
import Target.RVInstructions.RVMove;

import java.util.*;

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
            } return false;
        }
    }


    public RegAllocator(RVModule topModule) {
        super(topModule);
        this.adjacentSet = new HashSet<>();
        this.simplifyWorkList = new LinkedList<>();
        this.freezeWorkList = new LinkedList<>();
        this.spilledNodes = new LinkedList<>();
        this.spillWorkList = new LinkedList<>();
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
                Coalesce();;
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

    private void AssignColors() {

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
        for (RVFunction rvFunction : rvModule.rvFunctionList) {
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
        edge uv = new edge(u,v);
        if (!adjacentSet.contains(uv) && u != v) {
            adjacentSet.add(uv);
            adjacentSet.add(new edge(v,u));
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

    private HashSet<RVMove> getNodeMoves(VirtualReg n) {
        HashSet<RVMove> nodeMoves = new HashSet<>( moveList(n) );
        nodeMoves.removeIf(i -> {return activeMoves.contains(i) || workListMoves.contains(i); } );
        return nodeMoves;
    }

    private void MakeWorkList() {
        while (!initial.isEmpty()) {
            VirtualReg n =  initial.pop();
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

        }
    }

    private void EnableMoves(VirtualReg n) {

    }

    private void Simplify() {
        VirtualReg n = simplifyWorkList.pop();
        this.selectStack.push(n);
        for (VirtualReg m : n.neighbors) {

        }

    }

    private void Coalesce() {

    }

    private void Freeze() {

    }

    private void SelectSpill() {

    }

    private void RewriteProgram() {

    }





    @Override
    public Object visit(RVBlock rvBlock) {

        return null;
    }
}
