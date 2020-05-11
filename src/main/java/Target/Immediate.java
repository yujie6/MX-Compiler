package Target;

public class Immediate extends RVOperand {

    int value;

    public Immediate(int val) {
        this.value = val;
    }

    @Override
    public String toString() {
        if (value == 0) return "zero";
        return String.valueOf(value);
    }
}
