package IR.Types;

import java.util.ArrayList;

/**
 *  The structure type is used to represent a collection of data members together
 *  in memory. The elements of a structure may be any type that has a size.
 *
 *  Structures in memory are accessed using ‘load’ and ‘store’ by getting a pointer to a field with the ‘getelementptr’
 *  instruction. Structures in registers are accessed using the ‘extractvalue’ and ‘insertvalue’ instructions.
 *
 *  Structures may optionally be “packed” structures, which indicate that the alignment of the struct is one byte
 *  (Optional, I may not bother)
 */

public class StructureType extends AggregateType {
    private ArrayList<IRBaseType> MemberList;

    public StructureType(ArrayList<IRBaseType> typeList) {
        this.BaseTypeName = TypeID.StructTyID;
        this.MemberList = typeList;
        int sum = 0;
        for (IRBaseType typeMember : MemberList) {
            sum += typeMember.getBytes();
        }
        this.ByteNum = sum;
    }

    public ArrayList<IRBaseType> getMemberList() {
        return MemberList;
    }


}