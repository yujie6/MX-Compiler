package Target;

import java.util.Set;

public class RVTargetInfo {
    public static String[] regNames = {"zero", "ra", "sp", "gp", "tp", "t0", "t1", "t2", "s0" ,
            "s1", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10",
            "s11", "t3", "t4", "t5", "t6"};
    public static String[] allocatables = {"t0", "t1", "t2", "s0" ,
            "s1", "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10",
            "s11", "t3", "t4", "t5", "t6"};
    public static String[] calleeSaves = {"ra", "s0" }; // , "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11"};
    public static String[] argRegs = {"a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7"};
    // Up to eight integer registers, a0–a7
    // Arguments more than twice the size of a pointer-word are passed by reference.
    // In the standard RISC-V calling convention, the stack grows downward and the stack pointer is always kept 16-byte aligned.


    public static Set<String> callerSaves = Set.of("ra", "t0", "t1", "t2", "a0", "a1", "a2",
            "a3", "a4", "a5", "a6", "a7", "t3", "t4", "t5", "t6");
    public static String[] tmpRegs = {"t0", "t1", "t2", "t3", "t4", "t5", "t6"};

}
