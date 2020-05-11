package Target;

import IR.GlobalVariable;

/**
 * .globl i
 * Allocate 4 bytes in the globals segment for the variable i.
 * Can also be viewed as address!
 *
 * An absolute address is formed using two instructions, the U-Type lui
 * instruction to load bits[31:20] and an I-Type or S-Type instruction
 * such as addi, lw or sw that fills in the low 12 bits relative to the upper immediate.
 */
public class RVGlobal extends RVOperand { // can extends RVGlobal
    private String identifier, stringValue;
    public boolean isStringConst;
    private int strSize;
    public RVGlobal(GlobalVariable gvar) {
        this.identifier = gvar.getIdentifier();
        this.isStringConst = gvar.isStringConst;
        this.stringValue = isStringConst ? gvar.getInitValue().toString() : null;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder(identifier);
        ans.append("\n.asciz\t").append(this.stringValue);
        ans.append("\n\t").append(this.stringValue.length()).append('\n');
        return ans.toString();
    }

    public String getIdentifier() {
        return identifier;
    }
}
