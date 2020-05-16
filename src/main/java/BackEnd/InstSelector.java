package BackEnd;


import IR.*;
import IR.Constants.Constant;
import IR.Constants.IntConst;
import IR.Instructions.*;
import IR.Module;
import Target.*;
import Target.RVInstructions.*;
import Tools.MXLogger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Simple and stupid
 * TODO: getPtr & bitcast
 */
public class InstSelector implements IRVisitor {

    private RVModule riscvTopModule;
    private Module IRModule;
    private MXLogger logger;
    private RVFunction curFunction;
    private RVBlock curBlock;
    private HashMap<BasicBlock, RVBlock> rvBlockMap;
    private HashMap<Function, RVFunction> rvFunctionMap;
    private HashMap<Instruction, VirtualReg> virtualRegMap;
    private HashMap<Instruction, RVAddr> rvAddrMap;
    private HashMap<GlobalVariable, RVGlobal> globalVarMap;
    private HashMap<Argument, RVAddr> argumentMap;
    private HashMap<VirtualReg, VirtualReg> calleeSavedMap;
    static public HashMap<String, VirtualReg> fakePhyRegMap;
    static final private Immediate ZERO = new Immediate(0);


    public InstSelector(Module IRModule, MXLogger logger) {
        this.riscvTopModule = new RVModule();
        this.IRModule = IRModule;
        this.logger = logger;
        this.rvAddrMap = new HashMap<>();
        this.rvBlockMap = new HashMap<>();
        this.rvFunctionMap = new HashMap<>();
        this.virtualRegMap = new HashMap<>();
        this.calleeSavedMap = new HashMap<>();
        fakePhyRegMap = new HashMap<>();
        this.argumentMap = new HashMap<>();
        this.globalVarMap = new HashMap<>();
        for (String name : RVTargetInfo.regNames) {
            VirtualReg fakeReg = new VirtualReg(name, true);
            fakePhyRegMap.put(name, fakeReg);
        }
        for (Value v : IRModule.getGlobalVarMap().values()) {
            GlobalVariable gvar = (GlobalVariable) v;
            if (!((GlobalVariable) v).isStringConst) {
                RVGlobal rvGlobal = new RVGlobal(gvar);
                this.riscvTopModule.addGlobalVar(rvGlobal);
                this.globalVarMap.put(gvar, rvGlobal);
            }
        }
        for (var entry : IRModule.getStringConstMap().entrySet()) {
            RVGlobal rvGlobal = new RVGlobal(entry.getValue());
            rvGlobal.setStringValue(entry.getKey());
            this.riscvTopModule.addGlobalVar(rvGlobal);
            this.globalVarMap.put(entry.getValue(), rvGlobal);
        }
        for (Function function : IRModule.getFunctionMap().values()) {
            this.argumentMap.clear();
            if (function.isExternal()) {
                continue;
            }
            visit(function);
        }
    }

    public RVModule getRISCVTopModule() {
        return riscvTopModule;
    }

    private VirtualReg getVirtualReg(Value val) {
        if (val instanceof Instruction) {
            return getVirtualReg((Instruction) val);
        } else if (val instanceof IntConst) {
            return getVirtualReg((IntConst) val);
        } else {
            logger.severe("Do not support this type!!");
            return null;
        }
    }

    private VirtualReg getVirtualReg(Instruction inst) {
        if (this.virtualRegMap.containsKey(inst)) {
            return this.virtualRegMap.get(inst);
        } else if (inst instanceof AllocaInst) {
            RVAddr addr = this.rvAddrMap.get(inst);
            VirtualReg tmp = new VirtualReg("tmp");
            curBlock.AddInst(new RVLoad(curBlock, tmp, addr));
            return tmp;
        } else {
            VirtualReg vReg = new VirtualReg(inst);
            virtualRegMap.put(inst, vReg);
            return vReg;
        }
    }

    private RVAddr getAddr(Instruction inst) {
        /*
         * Serve for alloca & getElementPtr
         */
        if (inst instanceof AllocaInst) {
            return rvAddrMap.get(inst);
        } else {
            logger.severe("Addr map does not contain such element!");
            return null;
        }
    }

    private VirtualReg getVirtualReg(IntConst intConst) {
        return getVirtualReg(new Immediate(intConst.ConstValue));
    }

    private VirtualReg getVirtualReg(Immediate imm) {
        VirtualReg tmp = new VirtualReg("hold immediate");
        LUI lui = new LUI(curBlock, tmp, imm);
        curBlock.AddInst(lui);
        return tmp;
    }

    private VirtualReg getFakeReg(String name) {
        return fakePhyRegMap.get(name);
    }

    private RVInstruction getMoveInst(RVOperand src, VirtualReg dest) {
        return new RVMove(curBlock, (VirtualReg) src, dest);
    }

    private RVBlock getRVBlock(BasicBlock BB) {
        return rvBlockMap.get(BB);
    }

    private RVFunction getRVFunction(Function function) {
        if (this.rvFunctionMap.containsKey(function)) {
            return this.rvFunctionMap.get(function);
        } else {
            RVFunction rvFunction = new RVFunction(function);
            this.rvFunctionMap.put(function, rvFunction);
            return rvFunction;
        }
    }

    private RVOperand getRVOperand(Value irValue) {
        if (irValue instanceof GlobalVariable) {
            if (!globalVarMap.containsKey(irValue)) {
                logger.severe("Global variable not stored!");
            }
            return this.globalVarMap.get(irValue);
        } else if (irValue instanceof Argument) {
            RVAddr addr = argumentMap.get(irValue);
            VirtualReg tmp = new VirtualReg("argument");
            curBlock.AddInst( new RVLoad(curBlock, tmp, addr));
            return tmp;
        } else if (irValue instanceof Instruction) {
            if (irValue instanceof AllocaInst) return rvAddrMap.get(irValue);
            return getVirtualReg((Instruction) irValue);
        } else if (irValue instanceof Constant) {
            // return imm
            if (irValue instanceof IntConst) {
                return new Immediate(((IntConst) irValue).ConstValue);
            }
        }
        return null;
    }

    private void updateStackAddr(RVAddr addr, int deltaStack) {
        addr.resetStackAddr(deltaStack);
    }

    @Override
    public Object visit(BasicBlock node) {
        RVBlock rvBlock = getRVBlock(node);
        curBlock = rvBlock;
        for (BasicBlock pred : node.predecessors) {
            rvBlock.predecessors.add(getRVBlock(pred));
        }
        for (BasicBlock succ : node.successors) {
            rvBlock.successors.add(getRVBlock(succ));
        }

        if (node.isEntryBlock()) {
            // Store argument on stack
            int index = 0;
            for (Argument arg : node.getParent().getParameterList()) {
                if (index < 8) {
                    RVAddr addr = new RVAddr(arg, curFunction);
                    RVStore rvStore = new RVStore(curBlock, getFakeReg("a" + index), addr);
                    argumentMap.put(arg, addr);
                    curBlock.AddInst(rvStore);

                } else {

                }
                index += 1;
            }
        }
        for (Instruction inst : node.getInstList()) {
            inst.accept(this);
        }

//        if (node.isEntryBlock()) {
//            Immediate deltaStack = new Immediate(curFunction.getDeltaStack());
//
//            for (var entry : this.rvAddrMap.entrySet()) {
//                if (entry.getKey() instanceof AllocaInst) {
//                    updateStackAddr(entry.getValue(), deltaStack.getValue());
//                }
//            }
//            for (RVAddr addr : this.argumentMap.values()) {
//                updateStackAddr(addr, deltaStack.getValue());
//            }
//        }

        return rvBlock;
    }

    @Override
    public Object visit(Function node) {
        RVFunction rvFunction = getRVFunction(node);
        curFunction = rvFunction;

        for (BasicBlock BB : node.getBlockList()) {
            RVBlock rvBlock = new RVBlock(BB);
            rvBlockMap.put(BB, rvBlock);
            rvFunction.addRVBlock(rvBlock);
        }
        this.rvAddrMap.clear();
        this.argumentMap.clear();
        this.virtualRegMap.clear();
        this.calleeSavedMap.clear();
        curBlock = rvBlockMap.get(node.getHeadBlock());
        for (String name : RVTargetInfo.calleeSaves) {
            VirtualReg backup = new VirtualReg("fake_" + name, false);
            VirtualReg calleeSavedReg = getFakeReg(name);
            this.calleeSavedMap.put(calleeSavedReg, backup);
            curBlock.AddInst(new RVMove(curBlock, calleeSavedReg, backup));
        }
        // TODO update s0 here
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
        rvAddrMap.put(allocaInst, addr);
        return addr;
    }

    @Override
    public Object visit(BinOpInst binOpInst) {
        RVOperand RHS = getRVOperand(binOpInst.getRHS());
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
                if (hasImm) {
                    if (RHS instanceof Immediate) RHS = getVirtualReg((Immediate) RHS);
                    if (LHS instanceof Immediate) LHS = getVirtualReg((Immediate) LHS);
                }
                curBlock.AddInst(new RVArith(RVOpcode.mul, curBlock, LHS, RHS, dest));
                break;
            }
            case sub: {
                if (hasImm) {
                    if (RHS instanceof Immediate) ((Immediate) RHS).setNegative();
                    if (LHS instanceof Immediate) ((Immediate) LHS).setNegative();
                    curBlock.AddInst(new RVArithImm(RVOpcode.addi, curBlock, LHS, RHS, dest));
                } else curBlock.AddInst(new RVArith(RVOpcode.sub, curBlock, LHS, RHS, dest));
                break;
            }
            case sdiv: {
                if (hasImm) {
                    if (RHS instanceof Immediate) RHS = getVirtualReg((Immediate) RHS);
                    if (LHS instanceof Immediate) LHS = getVirtualReg((Immediate) LHS);
                }
                curBlock.AddInst(new RVArith(RVOpcode.div, curBlock, LHS, RHS, dest));
                break;
            }
            case srem: {
                if (hasImm) {
                    if (RHS instanceof Immediate) RHS = getVirtualReg((Immediate) RHS);
                    if (LHS instanceof Immediate) LHS = getVirtualReg((Immediate) LHS);
                }
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
        if (branchInst.isHasElse()) {
            Value cond = branchInst.getCondition();
            if (cond instanceof CmpInst) {
                VirtualReg LHS = getVirtualReg(((CmpInst) cond).getLHS());
                VirtualReg RHS = getVirtualReg(((CmpInst) cond).getRHS());
                switch (((CmpInst) cond).SubOpcode) {
                    case eq:
                        curBlock.AddInst(new RVBranch(RVOpcode.beq, curBlock, LHS, RHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    case ne:
                        curBlock.AddInst(new RVBranch(RVOpcode.bne, curBlock, LHS, RHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    case ugt:
                        curBlock.AddInst(new RVBranch(RVOpcode.bltu, curBlock, RHS, LHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    case uge:
                        curBlock.AddInst(new RVBranch(RVOpcode.bgeu, curBlock, LHS, RHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    case ult:
                        curBlock.AddInst(new RVBranch(RVOpcode.bltu, curBlock, LHS, RHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    case ule:
                        curBlock.AddInst(new RVBranch(RVOpcode.bgeu, curBlock, RHS, LHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    case sgt:
                        curBlock.AddInst(new RVBranch(RVOpcode.blt, curBlock, RHS, LHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    case sge:
                        curBlock.AddInst(new RVBranch(RVOpcode.bge, curBlock, LHS, RHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    case slt:
                        curBlock.AddInst(new RVBranch(RVOpcode.blt, curBlock, LHS, RHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    case sle:
                        curBlock.AddInst(new RVBranch(RVOpcode.bge, curBlock, RHS, LHS, getRVBlock(branchInst.getThenBlock())));
                        curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + ((CmpInst) cond).SubOpcode);
                }
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

        for (Value arg : callInst.getArgumentList()) {
            RVOperand argument = getRVOperand(arg);
            if (index < 8) {
                curBlock.AddInst(getMoveInst(argument, getFakeReg("a" + index)));
            } else {
                // store in memory
            }
            index++;
        }
        curBlock.AddInst(new RVCall(curBlock, getRVFunction(callInst.getCallee())));
        if (!callInst.isVoid()) {
            // VirtualReg callRetVal = getVirtualReg(callInst);
            this.virtualRegMap.put(callInst, getFakeReg("a0"));
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
        RVOperand baseAddr = getRVOperand(getPtrInst.getAggregateValue());
        if (baseAddr instanceof RVGlobal) {
            if (!getPtrInst.hasAllZeroOffsets()) {

            }
            VirtualReg tmp = new VirtualReg("tmp for abs addr");
            LUI lui = new LUI(curBlock, tmp, baseAddr);
            RVArithImm addi = new RVArithImm(RVOpcode.addi, curBlock, tmp, baseAddr, tmp);

            curBlock.AddInst(lui);
            curBlock.AddInst(addi);
            this.virtualRegMap.put(getPtrInst, tmp);
            return null;
        }
        int s = getPtrInst.getTotalOffset();
        VirtualReg tmp = new VirtualReg("tmp for addr computation");
        if (s != -42) {
            RVArithImm addi = new RVArithImm(RVOpcode.addi, curBlock, baseAddr, new Immediate(s), tmp);
            curBlock.AddInst(addi);
            this.virtualRegMap.put(getPtrInst, tmp);
            return null;
        } else {
            logger.severe("Not handled");
            // RVArith add = new RVArith(RVOpcode.add, curBlock, baseAddr, )
        }
        // RVAddr addr = new RVAddr(baseAddr, );

        return null;
    }

    // TODO : process global var & getptr inst...
    @Override
    public Object visit(LoadInst loadInst) {
        RVOperand addr = getRVOperand(loadInst.getLoadAddr());
        if (addr instanceof RVGlobal) {
            VirtualReg tmp = new VirtualReg("tmp for abs addr");
            LUI lui = new LUI(curBlock, tmp, addr);
            addr = new RVAddr((RVGlobal) addr, tmp); // should load
            curBlock.AddInst(lui);
        } else if (!(addr instanceof RVAddr)) {
            logger.severe("load address not processed!");
        }
        VirtualReg dest = getVirtualReg(loadInst);
        curBlock.AddInst(new RVLoad(curBlock, dest, (RVAddr) addr));
        return null;
    }

    @Override
    public Object visit(ReturnInst returnInst) {
        // TODO finally load callee saved registers from stack
        if (!returnInst.getRetType().isVoidType()) {
            curBlock.AddInst(new RVMove(curBlock, (VirtualReg) getRVOperand(returnInst.getRetValue()),
                    getFakeReg("a0")));
        }
        for (var entry : calleeSavedMap.entrySet()) {
            curBlock.AddInst(new RVMove(curBlock, entry.getValue(), entry.getKey()));
        }
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
        if (destAddr instanceof RVGlobal) {
            VirtualReg tmp = new VirtualReg("tmp for abs addr");
            LUI lui = new LUI(curBlock, tmp, destAddr);
            destAddr = new RVAddr((RVGlobal) destAddr, tmp); // should load
            curBlock.AddInst(lui);
        }
        curBlock.AddInst(new RVStore(curBlock, src, destAddr));

        return null;
    }
}
