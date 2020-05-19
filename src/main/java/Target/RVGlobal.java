package Target;

import IR.Constants.IntConst;
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
    public boolean isStringConst, hasInitValue = false;
    private int initValue;
    public RVGlobal(GlobalVariable gvar) {
        this.identifier = gvar.getIdentifier();
        this.isStringConst = gvar.isStringConst;
        if (gvar.getInitValue() instanceof IntConst) {
            this.hasInitValue = true;
            this.initValue = ((IntConst) gvar.getInitValue()).ConstValue;
        }
        this.stringValue = isStringConst ? gvar.getInitValue().toString() : null;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();
        if (isStringConst) {
            // ans.append("\t.globl\t").append(identifier).append("\t\t\t#@").append(identifier).append("\n");
            ans.append("\t.type\t").append(identifier).append(",@object\t\t\t#@").append(identifier).append("\n");
            ans.append("\t.section\t.rodata\n");
            ans.append(this.getIdentifier()).append(":\n");
            ans.append("\t.asciz\t").append(asmTransform(this.stringValue)).append("\n");
            ans.append("\t.size\t").append(identifier).append(",\t").append(this.stringValue.length() - 1).append("\n\n");
        } else {
            ans.append("\t.globl\t").append(identifier).append("\t\t\t#@").append(identifier).append("\n");
            ans.append("\t.p2align\t2").append("\n");

            ans.append(this.getIdentifier()).append(":\n");
            if (hasInitValue) {
                ans.append("\t.word\t").append(initValue).append("\n");
            } else ans.append("\t.word\t").append(0).append("\n");
            // ans.append("\t.size\t").append(4).append("\n\n");
        }
        return ans.toString();
    }

    public static String asmTransform(String in) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            switch (in.charAt(i)) {
                case '\n':
                    ret.append("\\n");
                    break;
                case '\t':
                    ret.append("\\t");
                    break;
                case '\\':
                    ret.append("\\\\");
                    break;
                case '\0':
                    ret.append("\\0");
                    break;
                default:
                    ret.append(in.charAt(i));
                    break;
            }
        }
        return ret.toString();
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }
}
