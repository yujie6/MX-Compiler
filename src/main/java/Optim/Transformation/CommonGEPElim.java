package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.GetPtrInst;
import IR.Instructions.Instruction;
import IR.Instructions.LoadInst;
import IR.User;
import IR.Value;
import Optim.FuncAnalysis.AliasAnalysis;
import Optim.FuncAnalysis.DomNode;
import Optim.FuncAnalysis.DomTreeBuilder;
import Optim.FunctionPass;
import Optim.MxOptimizer;

import java.util.*;

/**
 * Eliminate useless getElementPtr, together with redundant Load elimination
 * To do this, we have to compute the reaching definition
 */
public class CommonGEPElim extends FunctionPass {

    private AliasAnalysis AA;
    private DomTreeBuilder dm;
    private int elimNum = 0;
    private class gepExpr {
        String base;
        ArrayList<String> offsets;
        public gepExpr(GetPtrInst gep) {
            offsets = new ArrayList<>();
            base = gep.getAggregateValue().toString();
            for (Value offset: gep.getOffsets()) {
                offsets.add(offset.toString());
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            gepExpr gepExpr = (gepExpr) o;
            return Objects.equals(base, gepExpr.base) &&
                    Objects.equals(offsets, gepExpr.offsets);
        }

        @Override
        public int hashCode() {
            return Objects.hash(base, offsets);
        }
    }

    private HashMap<gepExpr, GetPtrInst> gepMap;
    public CommonGEPElim(Function function, AliasAnalysis AA, DomTreeBuilder dm) {
        super(function);
        this.AA = AA;
        this.dm = dm;
        this.gepMap = new HashMap<>();
    }

    @Override
    public boolean optimize() {
        gepMap.clear();
        elimNum = 0;
        visit(dm.domTree.get(function.getHeadBlock()));
        if (elimNum != 0)
            MxOptimizer.logger.fine(String.format("Common getPtrInst elimination works on function \"%s\", with %d GEP eliminated.",
                    function.getIdentifier(), elimNum));
        return elimNum != 0;
    }

    private void visit(DomNode domNode) {
        BasicBlock BB = domNode.block;
        for (Instruction inst : List.copyOf(BB.getInstList()) ) {
            if (inst instanceof GetPtrInst) {
                gepExpr key = new gepExpr(((GetPtrInst) inst));
                if (gepMap.containsKey(key)) {
                    GetPtrInst replaceVal = gepMap.get(new gepExpr(((GetPtrInst) inst)));
                    if (dm.dominates(replaceVal, inst)) {
                        inst.replaceAllUsesWith(replaceVal);
                        inst.eraseFromParent();
                        elimNum += 1;
                    } else {
                        gepMap.replace(key, ((GetPtrInst) inst));
                    }
                } else {
                    gepMap.put(key, ((GetPtrInst) inst));
                }
            }
        }
        domNode.children.forEach(child -> {visit(child);});
    }

}
