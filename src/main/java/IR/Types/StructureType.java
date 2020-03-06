package IR.Types;

import BackEnd.IRBuilder;
import IR.Value;

import java.util.ArrayList;
import java.util.HashMap;

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
    private String Identifier;
    private HashMap<Integer, IRBaseType> MemberTypeMap; // by offset
    private int MemberNum;
    public StructureType(String id, ArrayList<IRBaseType> typeList) {
        this.BaseTypeName = TypeID.StructTyID;
        this.Identifier = id;
        this.MemberList = typeList;
        MemberTypeMap = new HashMap<>();
        int sum = 0;
        MemberNum = 0;
        for (IRBaseType typeMember : MemberList) {
            MemberTypeMap.put(MemberNum, typeMember);
            MemberNum += 1;
            sum += typeMember.getBytes();
        }

        this.ByteNum = sum;
    }

    public ArrayList<IRBaseType> getMemberList() {
        return MemberList;
    }

    public void AddMemberType(IRBaseType memberType) {
        this.MemberList.add(memberType);
    }

    public String getIdentifier() {
        return Identifier;
    }

    @Override
    public Value getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder("%class.");
        ans.append(getIdentifier()).append(" = type { ");
        for (IRBaseType memberType : MemberList) {
            ans.append(memberType.toString()).append(", ");
        }
        ans.delete(ans.length()-3, ans.length()-1);
        ans.append("} ");
        return ans.toString();
    }

    @Override
    public IRBaseType getElementType(ArrayList<Value> offsets) {
        return null;
    }

    public IRBaseType getElementType(int offset) {
        return MemberTypeMap.get(offset);
    }
}
