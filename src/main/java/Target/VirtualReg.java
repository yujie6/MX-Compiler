package Target;

import IR.Instructions.Instruction;

import java.util.Arrays;
import java.util.HashSet;

public class VirtualReg extends RVOperand {

    String identifier;
    public int degree;
    public HashSet<VirtualReg> neighbors;
    public VirtualReg alias;
    public int color;

    public VirtualReg(Instruction inst) {
        this.identifier = inst.getRightValueLabel(inst);
        this.neighbors = new HashSet<>();
        this.degree = 0;
    }

    public VirtualReg(String name) {
        this.identifier = name;
    }

    public void addNeighbor(VirtualReg neighbor) {
        this.neighbors.add(neighbor);
        this.degree ++;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isPreColored() {
        return (new HashSet<>(Arrays.asList(RVTargetInfo.calleeSaves))).contains(identifier);
    }
}
