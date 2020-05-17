package BackEnd;

import IR.BasicBlock;
import IR.Instructions.Instruction;
import Target.*;
import Target.RVInstructions.*;
import com.ibm.icu.util.VTimeZone;

import java.util.*;

import static Optim.MxOptimizer.logger;

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
    private LivenessBuilder livenessBuilder;

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
    private int K; // allocatable registers
    private HashMap<VirtualReg, LinkedList<RVMove>> moveListMap;
    private RVFunction curFunction;
    private HashSet<PhyReg> allocatableRegs;
    private HashMap<String, PhyReg> phyRegMap;


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

        this.activeMoves = new HashSet<>();
        this.coalescedMoves = new HashSet<>();
        this.constrainedMoves = new HashSet<>();
        this.frozenMoves = new HashSet<>();

        this.moveListMap = new HashMap<>();
        this.workListMoves = new LinkedList<>();
        this.initial = new LinkedList<>();
        this.selectStack = new Stack<>();
        this.preColored = new HashSet<>(InstSelector.fakePhyRegMap.values());
        this.allocatableRegs = new HashSet<>();
        this.phyRegMap = new HashMap<>();
        for (String name : RVTargetInfo.allocatables) {
            PhyReg t = new PhyReg(name);
            allocatableRegs.add(t);
            this.phyRegMap.put(name, t);
        }
        this.K = this.allocatableRegs.size();
        this.phyRegMap.put("sp", new PhyReg("sp"));
        this.phyRegMap.put("ra", new PhyReg("ra"));
        this.phyRegMap.put("tp", new PhyReg("tp"));
        this.phyRegMap.put("gp", new PhyReg("gp"));
        this.preColored.forEach(v -> {v.color = phyRegMap.get(v.getIdentifier());} );
        this.livenessBuilder = new LivenessBuilder(TopModule);
        this.livenessBuilder.run();
    }

    @Override
    public void run() {
        visit(TopModule);

    }

    private PhyReg getPhyReg(String name) {
        return this.phyRegMap.get(name);
    }

    private boolean processDone() {
        return simplifyWorkList.isEmpty() && workListMoves.isEmpty()
                && freezeWorkList.isEmpty() && spillWorkList.isEmpty();
    }

    @Override
    public Object visit(RVFunction rvFunction) {
        curFunction = rvFunction;
        logger.fine("Running on function " + rvFunction.getIdentifier() + " once");
        clear();
        Build();
        MakeWorkList();
        do {
            if (!simplifyWorkList.isEmpty()) {
                Simplify();
            } else if (!workListMoves.isEmpty()) {
                Coalesce();
            } else if (!freezeWorkList.isEmpty()) {
                Freeze();
            } else if (!spillWorkList.isEmpty()) {
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
        for (RVBlock BB : curFunction.getRvBlockList()) {
            for (RVInstruction inst : BB.rvInstList) {
                initial.addAll(inst.getUseRegs());
                initial.addAll(inst.getDefRegs());
            }
        }
        initial.removeAll(preColored);

        this.adjacentSet.clear();
        this.workListMoves.clear();

        this.spillWorkList.clear();
        this.simplifyWorkList.clear();
        this.freezeWorkList.clear();

        this.activeMoves.clear();
        this.coalescedMoves.clear();
        this.constrainedMoves.clear();
        this.frozenMoves.clear();

        this.coloredNodes.clear();
        this.selectStack.clear();
        this.spilledNodes.clear();
        this.coalescedNodes.clear();
        this.moveListMap.clear();

        for (VirtualReg t : initial) {
            t.spillCost = 0;
            t.degree = 0;
            t.neighbors.clear();
            t.alias = null;
            t.color = null;
        }
    }

    private void setUpStack(RVFunction rvFunction) {
        curFunction = rvFunction;
        if (curFunction.getDeltaStack() == 0) {
            for (RVInstruction inst : rvFunction.getHeadBlock().rvInstList) {
                if (inst instanceof RVArithImm) {
                    RVArithImm addi = ((RVArithImm) inst);
                    if (addi.isStoreSP() ) {
                        addi.eraseFromParent();
                        break;
                    }
                }
            }
            return;
        }

        PhyReg sp = getPhyReg("sp");
        Immediate deltaStack = new Immediate(curFunction.getDeltaStack());
        RVBlock curBlock = curFunction.getRvBlockList().get(0);
        RVArithImm setStack = new RVArithImm(RVOpcode.addi, curBlock, sp, deltaStack, sp);
        deltaStack = new Immediate(-curFunction.getDeltaStack());

        for (RVInstruction inst : curBlock.rvInstList) {
            if (inst instanceof RVArithImm) {
                RVArithImm addi = ((RVArithImm) inst);
                if (addi.isStoreSP() ) {
                    addi.setImm(deltaStack);
                    break;
                }
            }
        }

        curBlock.AddInstAtTop(setStack);

        RVArithImm resetStack = new RVArithImm(RVOpcode.addi, curBlock, sp, deltaStack, sp);
        curBlock = curFunction.getRetBlock();
        RVInstruction ret = curBlock.rvInstList.getLast();
        ret.insertBeforeMe(resetStack);
    }


    @Override
    public Object visit(RVModule rvModule) {
        for (RVFunction rvFunction : rvModule.rvFunctions) {
            visit(rvFunction);
            setUpStack(rvFunction);
        }
        return null;
    }

    private void Build() {

        for (RVBlock BB : curFunction.getRvBlockList()) {
            BasicBlock irBB = BB.irBlock;
            int depth = curFunction.LA.getLoopDepth(irBB);
            for (RVInstruction inst : BB.rvInstList) {
                for (VirtualReg use : inst.getUseRegs()) {
                    use.spillCost += Math.pow(10, depth);
                }
                for (VirtualReg def : inst.getDefRegs()) {
                    def.spillCost += Math.pow(10, depth);
                }
            }

            HashSet<VirtualReg> live = new HashSet<>(BB.liveOutSet);
            for (int i = BB.rvInstList.size() - 1; i >= 0; i--) {
                RVInstruction inst = BB.rvInstList.get(i);
                if (inst instanceof RVMove) {
                    live.removeAll(inst.getUseRegs());
                    inst.getDefRegs().forEach(v -> {moveList(v).add((RVMove) inst);});
                    inst.getUseRegs().forEach(v -> {moveList(v).add((RVMove) inst);});
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
        return new HashSet<>(n.neighbors) {{
            removeAll(selectStack); removeAll(coalescedNodes);
        }};
    }

    private HashSet<RVMove> getNodeMoves(VirtualReg n) {
        return new HashSet<>(moveList(n)) {{
            removeIf(i -> { return !activeMoves.contains(i) && ! workListMoves.contains(i); });
        }};
        // moveList \cap  (activeMoves \cup workListMoves)
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
                if (activeMoves.contains(m)) {
                    activeMoves.remove(m);
                    workListMoves.add(m);
                }
            }
        }
    }

    private void Simplify() {
        VirtualReg n = simplifyWorkList.pop();
        if (!this.selectStack.contains(n)) this.selectStack.push(n);
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
            n.alias = getAlias(n.alias);
            return n.alias;
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
        }
        return true;
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
            u = y;
            v = x;
        } else {
            u = x;
            v = y;
        }
        if (u == v) {
            coalescedMoves.add(m);
            AddWorkList(u);
        } else if (preColored.contains(v) || adjacentSet.contains(new edge(u, v))) {
            constrainedMoves.add(m);
            AddWorkList(u);
            AddWorkList(v);
        } else if (checkCombine(u, v)) {
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
            if (getAlias(y) == getAlias(u)) {
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
        VirtualReg favorite = null;
        var iter = spillWorkList.iterator();
        while (iter.hasNext()) {
            favorite = iter.next();
            if (!favorite.spillTemporary) {
                break;
            }
        }
        while (iter.hasNext()) {
            VirtualReg t = iter.next();
            if (!t.spillTemporary && t.getSpillCost() < favorite.getSpillCost()) {
                favorite = t;
            }
        }
        return favorite;
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
            Set<PhyReg> okColors = new HashSet<>(this.allocatableRegs);
            for (VirtualReg w : n.neighbors) {
                VirtualReg w_alias = getAlias(w);
                if (preColored.contains(w_alias) ||
                        coloredNodes.contains(w_alias)) {
                    okColors.remove(w_alias.color);
                }
            }
            if (okColors.isEmpty()) {
                spilledNodes.add(n);
            } else {
                coloredNodes.add(n);
                n.color = okColors.iterator().next();
            }
        }

        for (VirtualReg n : coalescedNodes) {
            n.color = getAlias(n).color;
        }

        for (RVMove mv : coalescedMoves) {
            if (mv.getDest().color == mv.getDefRegs().get(0).color)
                mv.eraseFromParent();
        }
    }

    private void RewriteProgram() {
        // allocate memory for each v in spilledNodes
        // create a new temporary vi for each use and def
        Set<VirtualReg> newTemps = new HashSet<>();
        PhyReg sp = getPhyReg("sp");
        for (VirtualReg v : spilledNodes) {
            v.stackAddress = new RVAddr(sp, curFunction.allocaOnStack());
        }

        for (RVBlock BB : curFunction.getRvBlockList()) {
            for (RVInstruction inst : BB.rvInstList) {
                if (inst.getDefRegs().size() == 1) {
                    VirtualReg dest = inst.getDefRegs().get(0);
                    getAlias(dest);
                }
            }
        }

        for (RVBlock BB : curFunction.getRvBlockList()) {
            for (RVInstruction inst : BB.rvInstList) {
                for (VirtualReg d : inst.getDefRegs()) {
                    if (d.stackAddress != null) {
                        VirtualReg tmp = new VirtualReg("tmp_for_store");
                        newTemps.add(tmp);
                        RVStore st = new RVStore(inst.getParentBB(), tmp, d.stackAddress);
                        inst.replaceDef(tmp);
                        inst.insertAfterMe(st);
                    }
                }
                for (VirtualReg u : inst.getUseRegs()) {
                    if (u.stackAddress != null) {
                        VirtualReg tmp = new VirtualReg("tmp_for_load");
                        newTemps.add(tmp);
                        RVLoad ld = new RVLoad(inst.getParentBB(), tmp, u.stackAddress);
                        inst.replaceUse(u, tmp);
                        inst.insertBeforeMe(ld);
                    }
                }
            }
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
