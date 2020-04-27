package Target.RVInstructions;

public enum RVOpcode {
    add, sub,
    slt, sltu,
    and, or, xor,
    sll, slr, sra,
    jal, jalr,
    auipc,
    addi, slti, sltiu, andi, xori, ori,
    xlli, srli, srai, lui,
    beq, bne, blt, bltu, bge, bgeu,
    lw, lh, lb, lhu, lbu,
    sw, sh, sb
}

