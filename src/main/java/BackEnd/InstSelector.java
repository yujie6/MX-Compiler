package BackEnd;


import IR.*;
import IR.Constants.BoolConst;
import IR.Constants.Constant;
import IR.Constants.IntConst;
import IR.Constants.NullConst;
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
    private HashMap<Argument, RVOperand> argumentMap;
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
        } else if (val instanceof NullConst) {
            return getFakeReg("zero");
        } else if (val instanceof Argument) {
            RVOperand argAddr = argumentMap.get(val);
            if (argAddr instanceof VirtualReg) return (VirtualReg) argAddr;
            else if (argAddr instanceof RVAddr) {
                VirtualReg tmp = new VirtualReg("argument");
                curBlock.AddInst( new RVLoad(curBlock, tmp, (RVAddr) argAddr));
                return tmp;
            } else {
                logger.severe("this should never happen!");
                return null;
            }
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
        if (imm.getValue() < 2048 && imm.getValue() > -2048) {
            RVArithImm addi = new RVArithImm(RVOpcode.addi, curBlock, getFakeReg("zero"), imm, tmp);
            curBlock.AddInst(addi);
        } else {
            LI li = new LI(curBlock, imm, tmp);
            curBlock.AddInst(li);
        }
        return tmp;
    }

    private VirtualReg getFakeReg(String name) {
        return fakePhyRegMap.get(name);
    }

    private RVInstruction getMoveInst(RVOperand src, VirtualReg dest) {
        if (src instanceof VirtualReg)
            return new RVMove(curBlock, (VirtualReg) src, dest);
        else if (src instanceof Immediate) {
            int s = ((Immediate) src).getValue();
            if (s > 2048 || s < -2048) {
              src = getVirtualReg((Immediate) src);
              return new RVArith(RVOpcode.add, curBlock, getFakeReg("zero"), src, dest);
            } else return new RVArithImm(RVOpcode.addi, curBlock, getFakeReg("zero"), (Immediate) src,
                        dest);
        }
        else {
            logger.severe("move inst operand wrong!");
            return null;
        }
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
            return getVirtualReg(irValue);
        } else if (irValue instanceof Instruction) {
            if (irValue instanceof AllocaInst) return rvAddrMap.get(irValue);
            return getVirtualReg((Instruction) irValue);
        } else if (irValue instanceof Constant) {
            // return imm
            if (irValue instanceof NullConst) {
                return new Immediate(0);
            }
            if (irValue instanceof IntConst) {
                if (((IntConst) irValue).ConstValue < 2048 && ((IntConst) irValue).ConstValue >= -2058)
                    return new Immediate(((IntConst) irValue).ConstValue);
                else {
                    return getVirtualReg(irValue);
                }
            }
            if (irValue instanceof BoolConst) {
                return new Immediate((Integer) ((BoolConst) irValue).constValue);
            }
        }
        return null;
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
            int argNum = node.getParent().getParameterList().size();
            for (int i = 8; i < argNum; i++ ) {
                Argument arg = node.getParent().getParameterList().get(i);
                VirtualReg tmp = new VirtualReg("tmp for argument");
                curBlock.AddInst(new RVLoad(curBlock, tmp, new RVAddr(getFakeReg("sp"), 4 * (i - 8), curFunction)));
                this.argumentMap.put(arg, tmp);
            }
            for (int i = 0; i < Math.min(8, argNum); i++ ) {
                Argument arg = node.getParent().getParameterList().get(i);
                RVAddr addr = new RVAddr(arg, curFunction);
                RVStore rvStore = new RVStore(curBlock, getFakeReg("a" + i), addr);
                argumentMap.put(arg, addr);
                curBlock.AddInst(rvStore);
            }
        }
        for (Instruction inst : node.getInstList()) {
            inst.accept(this);
        }
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
        VirtualReg backup = new VirtualReg("fake_ra", false);
        VirtualReg calleeSavedReg = getFakeReg("ra");
        this.calleeSavedMap.put(calleeSavedReg, backup);
        curBlock.AddInst(new RVMove(curBlock, calleeSavedReg, backup));

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
        if ((RHS instanceof Immediate) && (LHS instanceof Immediate)) {
            RHS = getVirtualReg((Immediate) RHS);
        }
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
                    if (RHS instanceof Immediate) {
                        ((Immediate) RHS).setNegative();
                        curBlock.AddInst(new RVArithImm(RVOpcode.addi, curBlock, LHS, RHS, dest));
                    } else if (LHS instanceof Immediate) {
                        LHS = getVirtualReg((Immediate) LHS);
                        curBlock.AddInst(new RVArith(RVOpcode.sub, curBlock, LHS, RHS, dest));
                    }
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
        VirtualReg tmp = new VirtualReg("bitcast");
        curBlock.AddInst(getMoveInst(getRVOperand(bitCastInst.getCastValue()),  tmp) );
        this.virtualRegMap.put(bitCastInst, tmp);
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
            } else {
                VirtualReg t = getVirtualReg(cond);
                curBlock.AddInst(new RVBranch(RVOpcode.bne, curBlock, t, getFakeReg("zero"),
                        getRVBlock(branchInst.getThenBlock())));
                curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getElseBlock())));
            }
        } else {
            curBlock.AddInst(new RVJump(RVOpcode.j, curBlock, getRVBlock(branchInst.getThenBlock())));
        }
        return null;
    }

    @Override
    public Object visit(CallInst callInst) {
        // perhaps the most complicated one
        int argNum = callInst.getArgumentList().size();
        for (int i = 0; i < Math.min(8, argNum); i++) {
            RVOperand argument = getRVOperand(callInst.getArgument(i));
            curBlock.AddInst(getMoveInst(argument, getFakeReg("a" + i)));
        }
        for (int i = 8; i < argNum; i++) {
            Value arg = callInst.getArgument(i);
            VirtualReg argument = getVirtualReg(callInst.getArgument(i));
            RVAddr addr = new RVAddr(callInst.getCallee().getParameterList().get(i), curFunction);
            curBlock.AddInst(new RVStore(curBlock, argument, addr));
        }
        curBlock.AddInst(new RVCall(curBlock, getRVFunction(callInst.getCallee())));
        if (!callInst.isVoid()) {
            // VirtualReg callRetVal = getVirtualReg(callInst);
            VirtualReg tmp = new VirtualReg("hold a0");
            curBlock.AddInst(getMoveInst(getFakeReg("a0"), tmp) );
            this.virtualRegMap.put(callInst, tmp);
        }
        return null;
    }

    @Override
    public Object visit(CmpInst cmpInst) {
        int index = cmpInst.getParent().getInstList().indexOf(cmpInst);
        if (cmpInst.getParent().getInstList().get(index + 1) instanceof BranchInst) {
            return null;
        }
        VirtualReg tmp = new VirtualReg("tmp for cmp");
        VirtualReg LHS = getVirtualReg(cmpInst.getLHS());
        VirtualReg RHS = getVirtualReg(cmpInst.getRHS());
        // could fix some cases of imm
        // boolean hasImm = (RHS instanceof Immediate) || (LHS instanceof Immediate);
        switch (cmpInst.SubOpcode) {
            case eq: {
                // use xor
                VirtualReg delta = new VirtualReg("A minus B");
                curBlock.AddInst(new RVArith(RVOpcode.sub, curBlock, LHS, RHS, delta));
                // curBlock.AddInst(new RVCmp(RVOpcode.seqz, curBlock, delta, tmp));
                curBlock.AddInst(new RVArithImm(RVOpcode.sltiu, curBlock, delta, new Immediate(1), tmp));
                break;
            }
            case ne: {
                VirtualReg delta = new VirtualReg("A minus B");
                curBlock.AddInst(new RVArith(RVOpcode.sub, curBlock, LHS, RHS, delta));
                // curBlock.AddInst(new RVCmp(RVOpcode.snez, curBlock, delta, tmp));
                curBlock.AddInst(new RVArith(RVOpcode.sltu, curBlock, getFakeReg("zero"), delta, tmp));
                break;
            }
            case sle: {
                curBlock.AddInst( new RVArith(RVOpcode.slt, curBlock, RHS, LHS, tmp) );
                // curBlock.AddInst( new RVCmp(RVOpcode.not, curBlock, tmp, tmp));
                curBlock.AddInst( new RVArithImm(RVOpcode.xori, curBlock, tmp, new Immediate(1), tmp));
                break;
            }
            case slt: {
                curBlock.AddInst( new RVArith(RVOpcode.slt, curBlock, LHS, RHS, tmp) );
                break;
            }
            case sge: {
                curBlock.AddInst( new RVArith(RVOpcode.slt, curBlock, LHS, RHS, tmp) );
                // curBlock.AddInst( new RVCmp(RVOpcode.not, curBlock, tmp, tmp));
                curBlock.AddInst( new RVArithImm(RVOpcode.xori, curBlock, tmp, new Immediate(1), tmp));
                break;
            }
            case sgt: {
                curBlock.AddInst( new RVArith(RVOpcode.slt, curBlock, RHS, LHS, tmp) );
                break;
            }

        }
        this.virtualRegMap.put(cmpInst, tmp);
        return null;
    }

    @Override
    public Object visit(CopyInst copyInst) {
        if (copyInst.isParallel) {
            logger.severe("SSA destruction fail");
        }
        if (copyInst.getSrc() instanceof NullConst) {
            copyInst.replaceSrc(new IntConst(0));
        }
        RVOperand src = getRVOperand(copyInst.getSrc());
        RVOperand dest = getRVOperand(copyInst.getDest());
        curBlock.AddInst(getMoveInst(src, (VirtualReg) dest));
        return null;
    }

    @Override
    public Object visit(GetPtrInst getPtrInst) {
        // another complicated one, compute the address by add i
        RVOperand baseAddr = getRVOperand(getPtrInst.getAggregateValue());
        if (baseAddr instanceof RVGlobal) {
            VirtualReg tmp = new VirtualReg("tmp for abs addr");

            LUI lui = new LUI(curBlock, tmp, baseAddr);
            RVArithImm addi = new RVArithImm(RVOpcode.addi, curBlock, tmp, baseAddr, tmp);
            curBlock.AddInst(lui);
            curBlock.AddInst(addi);
            /*LA la = new LA (curBlock, (RVGlobal) baseAddr, tmp);
            curBlock.AddInst(la);*/
            this.virtualRegMap.put(getPtrInst, tmp);
            return null;
        }
        int s = getPtrInst.getTotalOffset();
        VirtualReg tmp = new VirtualReg("tmp for addr computation");
        if (s != -42) {
            if (s > 2048 || s < -2048) {
                RVArith add = new RVArith(RVOpcode.add, curBlock, baseAddr, getVirtualReg(new Immediate(s)), tmp);
                curBlock.AddInst(add);
            } else {
                RVArithImm addi = new RVArithImm(RVOpcode.addi, curBlock, baseAddr, new Immediate(s), tmp);
                curBlock.AddInst(addi);
            }
            this.virtualRegMap.put(getPtrInst, tmp);
            return null;
        } else {
            if (getPtrInst.getOffsets().size() == 1 ) {
                Value offset = getPtrInst.getOffsets().get(0); // must be instruction (actually offset * 4)

                RVArithImm slli = new RVArithImm(RVOpcode.slli, curBlock, getVirtualReg(offset), new Immediate(2), tmp);
                RVArith add = new RVArith(RVOpcode.add, curBlock, baseAddr, tmp, tmp);

                curBlock.AddInst(slli);
                curBlock.AddInst(add);
                this.virtualRegMap.put(getPtrInst, tmp);
                return null;
            }
            logger.severe("not handled");
        }
        return null;
    }

    // TODO : process global var & getptr inst...
    @Override
    public Object visit(LoadInst loadInst) {
        RVOperand addr = getRVOperand(loadInst.getLoadAddr());
        if (addr instanceof RVGlobal) {
            VirtualReg tmp = new VirtualReg("tmp for abs addr");
            /*LUI lui = new LUI(curBlock, tmp, addr);
            addr = new RVAddr((RVGlobal) addr, tmp, curFunction); // should load
            curBlock.AddInst(lui);*/
            LA la = new LA(curBlock, (RVGlobal) addr, tmp);
            curBlock.AddInst(la);
            addr = new RVAddr(tmp, 0, curFunction);
        } else if (!(addr instanceof RVAddr)) {
            if (addr instanceof VirtualReg)
                addr = new RVAddr((VirtualReg) addr, 0, curFunction);
            else logger.severe("load address not processed!");
        }
        VirtualReg dest = getVirtualReg(loadInst);
        curBlock.AddInst(new RVLoad(curBlock, dest, (RVAddr) addr));
        return null;
    }

    @Override
    public Object visit(ReturnInst returnInst) {
        // TODO finally load callee saved registers from stack
        if (!returnInst.getRetType().isVoidType()) {
            curBlock.AddInst(getMoveInst(getRVOperand(returnInst.getRetValue()), getFakeReg("a0")));
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
        Value val = sextInst.getExtendValue();
        if (val instanceof Instruction) {
            this.virtualRegMap.put(sextInst, getVirtualReg(((Instruction) val)));
        } else if (val instanceof IntConst) {
            VirtualReg v = getVirtualReg(new Immediate(((IntConst) val).ConstValue));
            this.virtualRegMap.put(sextInst, v);
        }
        return null;
    }

    @Override
    public Object visit(StoreInst storeInst) {
        RVOperand src = getRVOperand(storeInst.getStoreValue());
        if (src instanceof Immediate) {
            src = getVirtualReg((Immediate) src);
        }
        RVOperand destAddr = getRVOperand(storeInst.getStoreDest());
        if (destAddr instanceof RVGlobal) {
            VirtualReg tmp = new VirtualReg("tmp for abs addr");
            /*LUI lui = new LUI(curBlock, tmp, destAddr);
            destAddr = new RVAddr((RVGlobal) destAddr, tmp, curFunction); // should load
            curBlock.AddInst(lui);*/
            LA la = new LA(curBlock, (RVGlobal) destAddr, tmp);
            destAddr = new RVAddr(tmp, 0, curFunction);
            curBlock.AddInst(la);
        } else if (storeInst.getStoreDest() instanceof Instruction) {
            destAddr = new RVAddr(getVirtualReg(storeInst.getStoreDest()), 0, curFunction);
        }
        curBlock.AddInst(new RVStore(curBlock, src, destAddr));

        return null;
    }
}
