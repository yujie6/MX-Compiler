package Target;

import IR.Instructions.Instruction;

import java.util.Arrays;
import java.util.HashSet;

public class VirtualReg extends RVOperand {

    public static int vRegNum = 0;
    String identifier;
    public int degree;
    private boolean preColored;
    public HashSet<VirtualReg> neighbors;
    public VirtualReg alias;
    public int color;
    private int RegID;

    public VirtualReg(Instruction inst) {
        this.identifier = inst.getRightValueLabel(inst);
        this.preColored = false;
        this.neighbors = new HashSet<>();
        this.degree = 0;
        this.RegID = vRegNum;
        vRegNum += 1;
    }

    public VirtualReg(String name) {
        this.identifier = name;
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
    }

    public void addNeighbor(VirtualReg neighbor) {
        this.neighbors.add(neighbor);
        this.degree ++;
    }

    @Override
    public String toString() {
        if (preColored) return this.identifier;
        return "x" + RegID;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isPreColored() {
        return preColored;
    }
}
