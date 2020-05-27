package Optim.FuncAnalysis;

import IR.BasicBlock;
import IR.Constants.IntConst;
import IR.Function;
import IR.Instructions.*;
import IR.Value;
import Optim.FunctionPass;

/**
 * several methods which are used to query whether or not two memory objects alias, whether
 * function calls can modify or read a memory object, etc. For all of these queries, memory
 * objects are represented as a pair of their starting address (a symbolic LLVM Value) and a static size.
 * <p>
 * Alias analysis information is initially computed for a static snapshot of the program,
 * but clients will use this information to make transformations to the code. All but the
 * most trivial forms of alias analysis will need to have their analysis results updated to
 * reflect the changes made by these transformations.
 */
public class AliasAnalysis extends FunctionPass {
    public enum AR {MustAlias, MayAlias, NoAlias}

    ;

    public enum ModRefInfo {NoModRef, Mod, Ref, ModRef}
    private DomTreeBuilder dm;
    public AliasAnalysis(Function function, DomTreeBuilder dm) {
        super(function);
        this.dm = dm;
    }

    public AR alias(Value v1, Value v2) {
        if (v1 == v2) {
            return AR.MustAlias;
        }  else if (!v1.getType().equals(v2.getType())) {
            return AR.NoAlias;
        } else if (v1 instanceof IntConst && v2 instanceof IntConst) {
            if (((IntConst) v1).ConstValue == ((IntConst) v2).ConstValue ) {
                return AR.MustAlias;
            } else return AR.NoAlias;
        } else if (v1 instanceof GetPtrInst && v2 instanceof GetPtrInst) {
            GetPtrInst gep1 = (GetPtrInst) v1, gep2 = (GetPtrInst) v2;
            if (gep1.offsetNum == gep2.offsetNum) {
                if (alias(gep1.getAggregateValue(), gep2.getAggregateValue()) != AR.MustAlias)
                    return AR.NoAlias;
                for (int i = 0; i < gep1.offsetNum; i++ ){
                    Value offset1 = gep1.getOffset(i);
                    Value offset2 = gep2.getOffset(i);
                    if (alias(offset1, offset2) != AR.MustAlias) {
                        return AR.NoAlias;
                    }
                }
                return AR.MustAlias;
            } else return AR.NoAlias;
        } else return AR.MayAlias;
    }

    public ModRefInfo getModRefInfo(Instruction inst, Value pointer) {
        if (inst instanceof StoreInst) {
            return getModRefInfo(((StoreInst) inst), pointer);
        } else if (inst instanceof LoadInst) {
            return getModRefInfo(((LoadInst) inst), pointer);
        } else if (inst instanceof CallInst) {
            return getModRefInfo(((CallInst) inst), pointer);
        } else {
            return ModRefInfo.NoModRef;
        }
    }

    public ModRefInfo getModRefInfo(StoreInst storeInst, Value pointer) {
        if (alias(storeInst.getStoreDest(), pointer) != AR.NoAlias) {
            return ModRefInfo.Mod;
        } else return ModRefInfo.NoModRef;
    }

    public ModRefInfo getModRefInfo(LoadInst loadInst, Value pointer) {
        if (alias(loadInst.getLoadAddr(), pointer) != AR.NoAlias) {
            return ModRefInfo.Ref;
        } else return ModRefInfo.NoModRef;

    }

    public ModRefInfo getModRefInfo(CallInst callInst, Value pointer) {
        return getModRefBehavior(callInst.getCallee());
    }

    public ModRefInfo getModRefBehavior(Function function) {
        if (function.isExternal()) {
            switch (function.getIdentifier()) {
                case "print":
                case "printInt":
                case "println":
                case "printlnInt":
                    return ModRefInfo.Mod;
                case "getInt":
                case "getString":
                case "_malloc_and_init":
                case "malloc":
                    return ModRefInfo.Ref;
                case "toString":
                case "__string_length":
                case "__string_substring":
                case "__string_concatenate":
                case "__string_equal":
                case "__string_notEqual":
                case "__string_lessThan":
                case "__string_greaterThan":
                case "__string_lessEqual":
                case "__string_greaterEqual":
                case "__string_ord":
                case "__string_parseInt":
                case "__array_size":
                    return ModRefInfo.NoModRef;
                default: {
                    System.err.println("Wrong function name");
                    System.exit(-1);
                    return null;
                }
            }
        } else return ModRefInfo.ModRef;
    }

    @Override
    public boolean optimize() {
        return false;
    }
}
