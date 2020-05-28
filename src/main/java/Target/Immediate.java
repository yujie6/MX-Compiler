package Target;

public class Immediate extends RVOperand {

    int value;

    public Immediate(int val) {
        this.value = val;
    }

    public int getValue() {
        return value;
    }

    public void setNegative() {
        this.value = - this.value;
    }

    @Override
    public String toString() {
//        if (!(value < 2048 && value > -2048)) {
//            return "lo(" + value + ")";
//        } else
            return String.valueOf(value);
    }
}
