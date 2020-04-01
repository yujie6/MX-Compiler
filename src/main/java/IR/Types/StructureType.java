package IR.Types;

import IR.IRVisitor;
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
    private ArrayList<IRBaseType> MemberTypeList;
    private ArrayList<String> MemberNameList;
    private String Identifier;
    private HashMap<Integer, IRBaseType> MemberTypeMap; // by offset
    private int MemberNum;
    public StructureType(String id) {
        this.BaseTypeName = TypeID.StructTyID;
        this.Identifier = id;
        this.MemberTypeList = null;
        this.MemberNameList = null;
    }

    public StructureType(String id, ArrayList<IRBaseType> typeList, ArrayList<String> nameList) {
        this.BaseTypeName = TypeID.StructTyID;
        this.Identifier = id;
        this.MemberTypeList = typeList;
        this.MemberNameList = nameList;
        MemberTypeMap = new HashMap<>();
        int sum = 0;
        MemberNum = 0;
        for (IRBaseType typeMember : MemberTypeList) {
            MemberTypeMap.put(MemberNum, typeMember);
            MemberNum += 1;
            sum += typeMember.getBytes();
        }

        this.ByteNum = sum;
    }

    public boolean isFakeType() {
        return this.MemberTypeList == null;
    }

    public ArrayList<IRBaseType> getMemberTypeList() {
        return MemberTypeList;
    }

    public void AddMemberType(IRBaseType memberType) {
        this.MemberTypeList.add(memberType);
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
        ans.append(getIdentifier());
        return ans.toString();
    }

    public String getDeclaration() {
        StringBuilder ans = new StringBuilder("%class.");
        ans.append(getIdentifier()).append(" = type { ");
        for (IRBaseType memberType : MemberTypeList) {
            ans.append(memberType.toString()).append(", ");
        }
        ans.delete(ans.length()-2, ans.length() - 1);
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

    public IRBaseType getElementType (String name) {
        int id = this.MemberNameList.indexOf(name);
        if (id == -1) {
            return null;
        }
        return this.MemberTypeList.get(id);
    }

    public int getMemberOffset(String name) {
        return MemberNameList.indexOf(name);
    }

    public ArrayList<String> getMemberNameList() {
        return MemberNameList;
    }
}
