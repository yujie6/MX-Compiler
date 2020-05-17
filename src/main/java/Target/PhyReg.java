package Target;

public class PhyReg extends VirtualReg {

    public PhyReg(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return this.identifier;
    }
}
