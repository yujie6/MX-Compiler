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
import Optim.Pass;

import java.util.HashMap;

public class CommonSubexElim extends Pass {

    private Function function;
    private HashMap<expr, Instruction> exprMap;
    private DomTreeBuilder dm;

    private class expr {
        String LHS, RHS, opcode;

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
    }

    public CommonSubexElim(Function function1, DomTreeBuilder dm1) {
        this.function = function1;
        this.exprMap = new HashMap<>();
        this.dm = dm1;
    }



    @Override
    public boolean optimize() {
        boolean changed = false;
        exprMap.clear();
        DomNode root = dm.domTree.get(function.getHeadBlock());
        return visit(root);
    }

    private boolean visit(DomNode node) {
        boolean changed = false;
        BasicBlock BB = node.block;

        for (Instruction inst : BB.getInstList()) {
            if (inst instanceof BinOpInst || inst instanceof CmpInst) {
                expr curExpr = new expr(inst);
                if (exprMap.containsKey(curExpr)) {
                    Instruction replaceVal = exprMap.get(curExpr);
                    BasicBlock defBB = replaceVal.getParent();
                    if (dm.dominates(defBB, BB)) {
                        inst.replaceAllUsesWith(replaceVal);
                    } else {
                        exprMap.replace(curExpr, inst);
                        changed = true;
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

        for (DomNode child : node.children) {
            changed |= visit(child);
        }
        return changed;
    }


}
