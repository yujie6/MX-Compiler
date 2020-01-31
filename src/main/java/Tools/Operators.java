package Tools;

public class Operators
{
    public enum PreFixOp{INC, DEC, POS, NEG, LOGIC_NOT, BITWISE_NOT, DEFAULT}
    public enum PostFixOp{INC, DEC, DEFAULT}
    public enum BinaryOp{ADD, SUB, MUL, DIV, MOD, SHL, SHR, GREATER_EQUAL ,
        LESS_EQUAL, GREATER, LESS, EQUAL, NEQUAL, BITWISE_AND, BITWISE_OR,
        BITWISE_XOR, LOGIC_AND, LOGIC_OR, DEFAULT}
}
