package IR;

import java.util.ArrayList;

/**
 * This defines the Use class.  The Use class represents the operand of an
 * instruction or some other User instance which refers to a Value.  The Use
 * class keeps the "use list" of the referenced value up to date.
 *
 * Pointer tagging is used to efficiently find the User corresponding to a Use
 * without having to store a User pointer in every Use. A User is preceded in
 * memory by all the Uses corresponding to its operands, and the low bits of
 * one of the fields (Prev) of the Use class are used to encode offsets to be
 * able to find that User given a pointer to any Use.
 */
public class Use {
    private Value Val;
    private Use prev, next;
    private User user;

    public Use(Value var, User user) {
        this.user = user;
        this.Val = var;
    }

    public Use getPrev() {
        return prev;
    }

    public Use getNext() {
        return next;
    }

    public Value getVal() {
        return Val;
    }

    public User getUser() {
        return user;
    }
}
