package Optim.Transformation;

import IR.BasicBlock;
import IR.Function;
import IR.Instructions.BinOpInst;
import IR.Instructions.CmpInst;
import IR.Instructions.Instruction;
import IR.Value;
import Optim.FuncAnalysis.DomNode;
import Optim.FuncAnalysis.DomTreeBuilder;
import Optim.FuncOptimManager;
import Optim.FunctionPass;
import Optim.MxOptimizer;
import Optim.Pass;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CommonSubexElim extends FunctionPass {

    private HashMap<expr, Instruction> exprMap;
    private DomTreeBuilder dm;

    private class expr {
        String LHS, RHS, opcode;  // shall add gep!

        public expr(Instruction inst, boolean reverse) {
            this(inst); // call another constructor
            if (reverse) {
                if (inst instanceof BinOpInst) {
                    opcode = inst.Opcode.toString();
                    LHS = ((BinOpInst) inst).getRHS().toString();
                    RHS = ((BinOpInst) inst).getLHS().toString();
                } else if (inst instanceof CmpInst) {
                    opcode = ((CmpInst) inst).getFullOpcode();
                    LHS = ((CmpInst) inst).getRHS().toString();
                    RHS = ((CmpInst) inst).getLHS().toString();
                }
            }
        }


        public expr(Instruction inst) {
            if (inst instanceof BinOpInst) {
                opcode = inst.Opcode.toString();
                LHS = ((BinOpInst) inst).getLHS().toString();
                RHS = ((BinOpInst) inst).getRHS().toString();
            } else if (inst instanceof CmpInst) {
                opcode = ((CmpInst) inst).getFullOpcode();
                LHS = ((CmpInst) inst).getLHS().toString();
                RHS = ((CmpInst) inst).getRHS().toString();
            }
        }



        public expr (String LHS, String RHS, String opcode) {
            this.LHS = LHS;
            this.RHS = RHS;
            this.opcode = opcode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            expr expr = (expr) o;
            return Objects.equals(LHS, expr.LHS) &&
                    Objects.equals(RHS, expr.RHS) &&
                    Objects.equals(opcode, expr.opcode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(LHS, RHS, opcode);
        }
    }

    public CommonSubexElim(Function function1, DomTreeBuilder dm1) {
        super(function1);
        this.exprMap = new HashMap<>();
        this.dm = dm1;
    }

    private int elimNum = 0;
    @Override
    public boolean optimize() {
        exprMap.clear();
        elimNum = 0;
        DomNode root = dm.domTree.get(function.getHeadBlock());
        visit(root);
        if (elimNum != 0)
            MxOptimizer.logger.fine(String.format("Common subexpression elimination works on \"%s\" with %d inst eliminated ",
                function.getIdentifier(), elimNum));
        return elimNum != 0;
    }

    private void visit(DomNode node) {
        BasicBlock BB = node.block;

        for (Instruction inst : List.copyOf(BB.getInstList())) {
            if (inst instanceof BinOpInst || inst instanceof CmpInst) {
                expr curExpr = new expr(inst);
                if (exprMap.containsKey(curExpr)) {
                    Instruction replaceVal = exprMap.get(curExpr);
                    if ( dm.dominates( replaceVal, inst) ) {
                        inst.replaceAllUsesWith(replaceVal);
                        inst.eraseFromParent();
                        elimNum += 1;
                    } else {
                        exprMap.replace(curExpr, inst);
                    }
                } else {
                    exprMap.put(curExpr, inst);
                    if (inst.isCommutative()) {
                        expr reverseExpr = new expr(inst, true);
                        exprMap.put(reverseExpr, inst);
                    }
                }
            }
        }
        node.children.forEach( child -> {visit(child);});
    }


}
