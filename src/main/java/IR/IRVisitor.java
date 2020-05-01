package IR;

import IR.Instructions.*;

public interface IRVisitor<T> {
    T visit(BasicBlock node);
    T visit(Function node);
    T visit(AllocaInst allocaInst);
    T visit(BinOpInst binOpInst);
    T visit(BitCastInst bitCastInst);
    T visit(BranchInst branchInst);
    T visit(CallInst callInst);
    T visit(CmpInst cmpInst);
    T visit(CopyInst copyInst);
    T visit(GetPtrInst getPtrInst);
    T visit(LoadInst loadInst);
    T visit(ReturnInst returnInst);
    T visit(PhiInst phiInst);
    T visit(SextInst sextInst);
    T visit(StoreInst storeInst);
}
