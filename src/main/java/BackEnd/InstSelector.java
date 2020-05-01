package BackEnd;


import IR.*;
import IR.Constants.Constant;
import IR.Instructions.*;
import IR.Module;
import Target.*;
import Target.RVInstructions.*;
import Tools.MXLogger;
import javafx.scene.media.VideoTrack;

import java.util.HashMap;

/**
 * Maximal munch algorithm:
 */
public class InstSelector implements IRVisitor {

    private RVModule riscvTopModule;
    private Module IRModule;
    private MXLogger logger;
    private RVFunction curFunction;
    private RVBlock curBlock;
    private HashMap<BasicBlock, RVBlock> rvBlockMap;
    private HashMap<Instruction, VirtualReg> virtualRegMap;
    private HashMap<AllocaInst, RVAddr> allocaMap;
    private HashMap<String, VirtualReg> fakePhyRegMap;
    final private Immediate ZERO = new Immediate(0);


    public InstSelector(Module IRModule, MXLogger logger) {
        this.riscvTopModule = new RVModule();
        this.IRModule = IRModule;
        this.logger = logger;
        this.rvBlockMap = new HashMap<>();
        this.fakePhyRegMap = new HashMap<>();
        for (String name : RVTargetInfo.regNames) {
            VirtualReg fakeReg = new VirtualReg(name);
            fakePhyRegMap.put(name, fakeReg);
        }
        for (Function function : IRModule.getFunctionMap().values()) {
            if (function.isExternal()) {
                continue;
            }
            visit(function);
        }
    }

    public RVModule getRISCVTopModule() {
        return riscvTopModule;
    }

    private VirtualReg getVirtualReg(Instruction inst) {
        if (this.virtualRegMap.containsKey(inst)) {
            return this.virtualRegMap.get(inst);
        } else if (inst instanceof AllocaInst) {
             RVAddr addr = this.allocaMap.get(inst);
             VirtualReg tmp = new VirtualReg("tmp");
             curBlock.AddInst(new RVLoad(RVOpcode.lw, curBlock, tmp, addr));
             return tmp;
        } else {
            VirtualReg vReg = new VirtualReg(inst);
            virtualRegMap.put(inst, vReg);
            return vReg;
        }
    }

    private VirtualReg getFakeReg(String name) {
        return fakePhyRegMap.get(name);
    }

    private RVInstruction getMoveInst(RVOperand src, VirtualReg dest) {
        return new RVArithImm(RVOpcode.addi, curBlock, src, ZERO, dest);
    }

    private RVBlock getRVBlock(BasicBlock BB) {
        return rvBlockMap.get(BB);
    }

    private RVOperand getRVOperand(Value irValue) {
        if (irValue instanceof Instruction) {
            return getVirtualReg((Instruction) irValue);
        } else if (irValue instanceof Constant) {
            // return imm
        }
        return null;
    }

    @Override
    public Object visit(BasicBlock node) {
        RVBlock rvBlock = getRVBlock(node);
        for (Instruction inst : node.getInstList()) {
            inst.accept(this);
        }
        return rvBlock;
    }

    @Override
    public Object visit(Function node) {
        RVFunction rvFunction = new RVFunction(node);
        curFunction = rvFunction;
        for (BasicBlock BB : node.getBlockList()) {
            RVBlock rvBlock = new RVBlock(BB);
            rvFunction.addRVBlock(rvBlock);
        }
        for (BasicBlock BB : node.getBlockList()) {
            visit(BB);
        }
        this.riscvTopModule.addFunction(rvFunction);
        return rvFunction;
    }

    @Override
    public Object visit(AllocaInst allocaInst) {
        // allocate space on the stack
        RVAddr addr = new RVAddr(allocaInst, curFunction);
        allocaMap.put(allocaInst, addr);
        return addr;
    }

    @Override
    public Object visit(BinOpInst binOpInst) {
        RVOperand RHS = getRVOperand(binOpInst.getRHS() );
        RVOperand LHS = getRVOperand(binOpInst.getLHS());
        VirtualReg dest = getVirtualReg(binOpInst);
        boolean hasImm = (RHS instanceof Immediate) || (LHS instanceof Immediate);
        switch (binOpInst.Opcode) {
            case add: {
                if (hasImm) curBlock.AddInst(new RVArithImm(RVOpcode.addi, curBlock, LHS, RHS, dest));
                else curBlock.AddInst(new RVArith(RVOpcode.add, curBlock, LHS, RHS, dest));
                break;
            }
            case mul: {
                curBlock.AddInst(new RVArith(RVOpcode.mul, curBlock, LHS, RHS, dest));
                break;
            }
            case sub: {
                if (hasImm) curBlock.AddInst(new RVArithImm(RVOpcode.subi, curBlock, LHS, RHS, dest));
                else curBlock.AddInst(new RVArith(RVOpcode.sub, curBlock, LHS, RHS, dest));
                break;
            }
            case sdiv: {
                curBlock.AddInst(new RVArith(RVOpcode.div, curBlock, LHS, RHS, dest));
                break;
            }
            case srem: {
                curBlock.AddInst(new RVArith(RVOpcode.rem, curBlock, LHS, RHS, dest));
                break;
            }
            case and: {
                if (hasImm) curBlock.AddInst(new RVArithImm(RVOpcode.andi, curBlock, LHS, RHS, dest));
                else curBlock.AddInst(new RVArith(RVOpcode.and, curBlock, LHS, RHS, dest));
                break;
            }
            case xor: {
                if (hasImm) curBlock.AddInst(new RVArithImm(RVOpcode.xori, curBlock, LHS, RHS, dest));
                else curBlock.AddInst(new RVArith(RVOpcode.xor, curBlock, LHS, RHS, dest));
                break;
            }
            case or: {
                if (hasImm) curBlock.AddInst(new RVArithImm(RVOpcode.ori, curBlock, LHS, RHS, dest));
                else curBlock.AddInst(new RVArith(RVOpcode.or, curBlock, LHS, RHS, dest));
                break;
            }
            case shl: {
                if (hasImm) curBlock.AddInst(new RVArithImm(RVOpcode.slli, curBlock, LHS, RHS, dest));
                else curBlock.AddInst(new RVArith(RVOpcode.sll, curBlock, LHS, RHS, dest));
                break;
            }
            case shr: {
                if (hasImm) curBlock.AddInst(new RVArithImm(RVOpcode.srli, curBlock, LHS, RHS, dest));
                else curBlock.AddInst(new RVArith(RVOpcode.srl, curBlock, LHS, RHS, dest));
                break;
            }


            default: {
                logger.severe("opcode not implemented");
            }
        }
        return null;
    }

    @Override
    public Object visit(BitCastInst bitCastInst) {
        return null;
    }

    @Override
    public Object visit(BranchInst branchInst) {
        if (branchInst.isHasElse() ) {
            Value cond = branchInst.getCondition();
            if (cond instanceof CmpInst) {

            }
        } else {
            curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getThenBlock())));
        }
        return null;
    }

    @Override
    public Object visit(CallInst callInst) {
        // perhaps the most complicated one
        int index = 0;
        for (Value arg : callInst.getArgumentList() ) {
            RVOperand argument = getRVOperand(arg);
            if (index < 8) {
                curBlock.AddInst(getMoveInst(argument, getFakeReg("a" + index)));
            } else {
                // store in memory
            }
            index++;
        }
        return null;
    }

    @Override
    public Object visit(CmpInst cmpInst) {
        return null;
    }

    @Override
    public Object visit(CopyInst copyInst) {
        if (copyInst.isParallel) {
            logger.severe("SSA destruction fail");
        }
        RVOperand src = getRVOperand(copyInst.getSrc());
        RVOperand dest = getRVOperand(copyInst.getDest());
        curBlock.AddInst(new RVArithImm(RVOpcode.addi, curBlock, src, new Immediate(0), (VirtualReg) dest));
        return null;
    }

    @Override
    public Object visit(GetPtrInst getPtrInst) {
        // another complicated one, compute the address by add i


        return null;
    }

    @Override
    public Object visit(LoadInst loadInst) {
        RVOperand addr = getRVOperand(loadInst.getLoadAddr());
        VirtualReg dest = getVirtualReg(loadInst);
        curBlock.AddInst(new RVLoad(RVOpcode.lw, curBlock, dest, addr));
        return null;
    }

    @Override
    public Object visit(ReturnInst returnInst) {
        curBlock.AddInst(new RVRet(curBlock));
        return null;
    }

    @Override
    public Object visit(PhiInst phiInst) {
        // this would never execute
        return null;
    }

    @Override
    public Object visit(SextInst sextInst) {
        return null;
    }

    @Override
    public Object visit(StoreInst storeInst) {
        RVOperand src = getRVOperand(storeInst.getStoreValue());
        RVOperand destAddr = getRVOperand(storeInst.getStoreDest());
        curBlock.AddInst(new RVStore(RVOpcode.sw, curBlock, src, destAddr));

        return null;
    }
}
