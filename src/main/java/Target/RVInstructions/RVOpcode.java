package Target.RVInstructions;

public enum RVOpcode {
    add, sub,
    mul, div, rem, // rv32 m extension
    slt, sltu,
    and, or, xor,
    sll, srl, sra,
    jal, jalr, j, call, // some fake instructions
    auipc, // used by call
    addi, slti, sltiu, andi, xori, ori, subi,
    slli, srli, srai, lui,
    beq, bne, blt, bltu, bge, bgeu,
    lw, lh, lb, lhu, lbu,
    sw, sh, ret, sb
}

