package Target;

import IR.Instructions.Instruction;

import java.util.Arrays;
import java.util.HashSet;

public class VirtualReg extends RVOperand {

    public static int vRegNum = 0;
    String identifier;
    public int degree = 0, spillCost = 0;
    protected boolean preColored;
    public boolean spillTemporary = false;
    public HashSet<VirtualReg> neighbors;
    public VirtualReg alias;
    public PhyReg color;
    protected int RegID;
    public RVAddr stackAddress;

    public VirtualReg(String name) {
        this.identifier = name;
        this.preColored = false;
        this.neighbors = new HashSet<>();
        this.degree = 0;
        this.RegID = vRegNum;
        vRegNum += 1;
    }

    public VirtualReg(Instruction inst) {
        this.identifier = Instruction.getRightValueLabel(inst);
        this.preColored = false;
        this.neighbors = new HashSet<>();
        this.degree = 0;
        this.RegID = vRegNum;
        vRegNum += 1;
    }

    public VirtualReg(String name, boolean preColored) {
        this.identifier = name;
        this.neighbors = new HashSet<>();
        this.degree = 0;
        this.preColored = preColored;
        if (!preColored) {
            this.RegID = vRegNum;
            vRegNum += 1;
        }
    }

    public void reset() {
        this.neighbors.clear();
        this.degree = 0;
        this.alias = null;
        this.stackAddress = null;
    }

    public void addNeighbor(VirtualReg neighbor) {
        this.neighbors.add(neighbor);
        this.degree ++;
    }

    @Override
    public String toString() {
        if (preColored) return this.identifier;
        if (this.color != null) return this.color.identifier;
        return "x" + RegID;
    }

    public int getSpillCost() {
        if (degree == 0) return 20;
        return spillCost / degree;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isPreColored() {
        return preColored;
    }
}
